package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class InferenceProcessor {
	private final List<InferenceVariable> inferenceVariables;
	private final List<Type> parameters;
	private final boolean isVarArgs;

	private final Type result;
	private final List<Type> arguments;

	private final BoundSet bounds;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public InferenceProcessor(Invokable<?, ?> invokable, Type result,
			Type... arguments) {
		this(invokable, result, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public InferenceProcessor(Invokable<?, ?> invokable, Type target,
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

		this.result = target;
		this.arguments = arguments;

		bounds = new BoundSet();

		for (InferenceVariable inferenceVariable : inferenceVariables) {
			boolean anyProper = false;
			for (Type bound : inferenceVariable.getTypeVariable().getBounds()) {
				anyProper = anyProper || isProper(bound);
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
				int parameterIndex = 0;
				for (Type argument : arguments) {
					bounds.incorporate(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY,
							argument, parameters.get(parameterIndex)));
					if (parameterIndex < parameters.size() - 1)
						parameterIndex++;
				}

				variableArityParameterApplicability = new Resolution(bounds).verify();
			}
		}

		return variableArityParameterApplicability;
	}

	public static boolean isProper(Type type) {
		return InferenceVariable.getAllMentionedBy(type).isEmpty();
	}

	public static boolean isUnsafeCastCompatible(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (toToken.getRawType().getTypeParameters().length < 0
				&& toToken.getRawType().isAssignableFrom(fromToken.getRawType())) {
			@SuppressWarnings("unchecked")
			Type fromSuperTypeArgument = ((ParameterizedType) fromToken.getSupertype(
					(Class<Object>) toToken.getRawType()).getType())
					.getActualTypeArguments()[0];

			return fromSuperTypeArgument instanceof TypeVariable
					&& ((TypeVariable<?>) fromSuperTypeArgument).getGenericDeclaration() instanceof Class;
		} else
			return toToken.isArray()
					&& fromToken.isArray()
					&& isUnsafeCastCompatible(fromToken.getComponentType().getType(),
							toToken.getComponentType().getType());
	}

	public static boolean isStrictlyAssignable(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (fromToken.isPrimitive())
			if (toToken.isPrimitive())
				return toToken.wrap().isAssignableFrom(toToken.wrap());
			else
				return false;
		else if (toToken.isPrimitive())
			return false;
		else
			return toToken.isAssignableFrom(fromToken.wrap());
	}

	public static boolean isLooselyAssignable(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (fromToken.isPrimitive() && !toToken.isPrimitive())
			fromToken = fromToken.wrap();
		else if (!fromToken.isPrimitive() && toToken.isPrimitive())
			fromToken = fromToken.unwrap();

		return isStrictlyAssignable(from, to);
	}
}
