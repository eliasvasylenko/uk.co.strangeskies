package uk.co.strangeskies.reflection;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class ApplicabilityVerifier {
	private final Resolver resolver;

	private final List<Type> parameters;
	private final boolean isVarArgs;

	private final Type returnType;
	private final List<Type> arguments;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public ApplicabilityVerifier(Invokable<?, ?> invokable, Type result,
			Type... arguments) {
		this(invokable, result, Arrays.asList(arguments));
	}

	public ApplicabilityVerifier(Invokable<?, ?> invokable, Type returnType,
			List<Type> arguments) {
		resolver = new Resolver();
		resolver.capture(invokable.getGenericDeclaration());

		TypeSubstitution resolver = new TypeSubstitution();
		for (InferenceVariable<?> variable : this.resolver
				.getInferenceVariables(invokable.getGenericDeclaration())) {
			resolver = resolver.where(variable.getTypeVariable(), variable);
		}

		parameters = Arrays
				.stream(invokable.getGenericDeclaration().getParameters())
				.map(Parameter::getParameterizedType).map(resolver::resolve)
				.collect(Collectors.toList());
		isVarArgs = invokable.getGenericDeclaration().isVarArgs();

		this.returnType = returnType;
		this.arguments = arguments;

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
							parameter = Types.getComponentType(parameter);
							nextParameter = null;
						}
					}
					resolver.incorporateConstraint(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY,
							argument, parameter));
				}

				System.out.println(resolver.infer());

				variableArityParameterApplicability = resolver.validate();
			}
		}

		return variableArityParameterApplicability;
	}
}
