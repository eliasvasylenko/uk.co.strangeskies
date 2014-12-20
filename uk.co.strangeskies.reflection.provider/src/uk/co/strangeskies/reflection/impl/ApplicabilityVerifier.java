package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.impl.ConstraintFormula.Kind;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class ApplicabilityVerifier {
	private final List<InferenceVariable> inferenceVariables;
	private final List<Type> parameters;
	private final boolean isVarArgs;

	private final Type returnType;
	private final List<Type> arguments;

	private final BoundSet bounds;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public ApplicabilityVerifier(Invokable<?, ?> invokable, Type result,
			Type... arguments) {
		this(invokable, result, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public ApplicabilityVerifier(Invokable<?, ?> invokable, Type returnType,
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
				anyProper = anyProper || Types.isProperType(bound);
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

				Resolver resolver = new Resolver(bounds, inferenceVariables);

				System.out.println(resolver.resolve());

				variableArityParameterApplicability = resolver.validate();
			}
		}

		return variableArityParameterApplicability;
	}

	public BoundSet getResultingBounds() {
		verifyVariableArityParameterApplicability();
		return bounds;
	}
}
