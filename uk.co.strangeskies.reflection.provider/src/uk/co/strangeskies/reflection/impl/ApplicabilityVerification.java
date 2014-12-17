package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.impl.ConstraintFormula.Kind;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class ApplicabilityVerification {
	private final List<InferenceVariable> inferenceVariables;
	private final List<Type> parameters;
	private final boolean isVarArgs;

	private final Type returnType;
	private final List<Type> arguments;

	private final BoundSet bounds;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public ApplicabilityVerification(Invokable<?, ?> invokable, Type result,
			Type... arguments) {
		this(invokable, result, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public ApplicabilityVerification(Invokable<?, ?> invokable, Type returnType,
			List<Type> arguments) {
		inferenceVariables = InferenceVariable
				.forList((TypeVariable<? extends Executable>[]) invokable
						.getTypeParameters());

		TypeResolver resolver = new TypeResolver();
		for (InferenceVariable variable : inferenceVariables) {
			resolver = resolver.where(variable.getTypeVariable(), variable);
		}

		parameters = invokable.getParameters().stream().map(Parameter::getType)
				.map(TypeToken::getType).map(resolver::resolveType)
				.collect(Collectors.toList());
		isVarArgs = invokable.isVarArgs();

		this.returnType = returnType;
		this.arguments = arguments;

		bounds = new BoundSet();

		for (InferenceVariable inferenceVariable : inferenceVariables) {
			boolean anyProper = false;
			for (Type bound : inferenceVariable.getBounds()) {
				anyProper = anyProper || InferenceVariable.isProperType(bound);
				bounds.incorporate().acceptSubtype(inferenceVariable, bound);
			}
			if (!anyProper)
				bounds.incorporate().acceptSubtype(inferenceVariable, Object.class);
		}
	}

	public boolean verifyStrictParameterApplicability() {
		if (strictParameterApplicability == null) {
			strictParameterApplicability = verifyLooseParameterApplicability();
			// TODO && make sure no boxing/unboxing occurs!
		}

		return strictParameterApplicability;
	}

	public boolean verifyLooseParameterApplicability() {
		if (looseParameterApplicability == null) {
			looseParameterApplicability = !isVarArgs
					&& verifyVariableArityParameterApplicability();
		}

		return looseParameterApplicability;
	}

	public boolean verifyVariableArityParameterApplicability() {
		if (variableArityParameterApplicability == null) {
			if (isVarArgs) {
				variableArityParameterApplicability = parameters.size() - 1 <= arguments
						.size();
			} else {
				variableArityParameterApplicability = parameters.size() == arguments
						.size();
			}

			if (variableArityParameterApplicability) {
				Iterator<Type> parameters = this.parameters.iterator();
				Type nextParameter = parameters.next();
				Type parameter = nextParameter;
				for (Type argument : arguments) {
					if (nextParameter != null) {
						parameter = nextParameter;
						if (parameters.hasNext())
							nextParameter = parameters.next();
						else if (isVarArgs) {
							parameter = TypeToken.of(parameter).getComponentType().getType();
							nextParameter = null;
						}
					}

					bounds.incorporate(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY,
							argument, parameter));
				}

				System.out.println(bounds);

				variableArityParameterApplicability = new Resolution(bounds,
						inferenceVariables).verify();
			}
		}

		return variableArityParameterApplicability;
	}

	public static boolean isExactlyAssignable(Type from, Type to) {
		if (from == null || to.equals(from)) {
			return true;
		} else if (from instanceof IntersectionType) {
			/*
			 * We must be able to assign from at least one member of the intersection
			 * type.
			 */
			return Arrays.stream(((IntersectionType) from).getTypes()).anyMatch(
					f -> isExactlyAssignable(f, to));
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			return Arrays.stream(((IntersectionType) to).getTypes()).allMatch(
					t -> isExactlyAssignable(from, t));
		} else if (to instanceof WildcardType) {
			/*
			 * This Should be taken care of bye the TypeToken check below, but
			 * currently there is a bug, so we provide a correct implementation here
			 * for the moment.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();
			if (lowerBounds.length == 0)
				return false;
			else
				return isExactlyAssignable(from, new IntersectionType(lowerBounds))
						&& isExactlyAssignable(from, new IntersectionType(
								((WildcardType) to).getUpperBounds()));
		} else
			return TypeToken.of(to).isAssignableFrom(TypeToken.of(from));
	}

	public static boolean isStrictlyAssignable(Type from, Type to) {
		TypeToken<?> toToken = TypeToken.of(to);
		TypeToken<?> fromToken = TypeToken.of(from);

		if (fromToken.isPrimitive())
			if (toToken.isPrimitive())
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (toToken.isPrimitive())
			return false;
		else
			return isExactlyAssignable(from, to);
	}

	public static boolean isLooselyAssignable(Type from, Type to) {
		TypeToken<?> toToken = TypeToken.of(to);
		TypeToken<?> fromToken = TypeToken.of(from);

		if (fromToken.isPrimitive() && !toToken.isPrimitive())
			fromToken = fromToken.wrap();
		else if (!fromToken.isPrimitive() && toToken.isPrimitive())
			fromToken = fromToken.unwrap();

		return isStrictlyAssignable(from, to);
	}

	public BoundSet getResultingBounds() {
		verifyVariableArityParameterApplicability();
		return bounds;
	}
}
