package uk.co.strangeskies.reflection;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InferenceVariable extends CaptureType implements Type {
	private final Resolver resolver;

	private InferenceVariable(Resolver resolver, String name, Type[] upperBounds) {
		super(name, upperBounds, new Type[0]);

		this.resolver = resolver;
	}

	public InferenceVariable() {
		this(new Type[0], new Type[0]);
	}

	public InferenceVariable(Type[] upperBounds, Type[] lowerBounds) {
		super("INF", upperBounds, lowerBounds);

		this.resolver = null;
	}

	public Resolver getResolver() {
		return resolver;
	}

	public static Map<TypeVariable<?>, InferenceVariable> capture(
			Resolver resolver, GenericDeclaration declaration) {
		List<TypeVariable<?>> declarationVariables;
		if (declaration instanceof Class)
			declarationVariables = Types.getAllTypeParameters((Class<?>) declaration);
		else
			declarationVariables = Arrays.asList(declaration.getTypeParameters());

		return CaptureType.capture(declarationVariables,
				t -> new InferenceVariable(resolver, t.getName(), t.getBounds()));
	}

	public static CaptureConversion capture(ParameterizedType type) {
		ParameterizedType originalType = type;

		/*
		 * There exists a capture conversion from a parameterized type G<T1,...,Tn>
		 * (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1 ≤ i ≤ n :
		 */

		Map<TypeVariable<?>, Type> parameterArguments = new HashMap<>();
		Map<InferenceVariable, Type> capturedArguments = new HashMap<>();
		Map<InferenceVariable, TypeVariable<?>> capturedParameters = new HashMap<>();

		Map<TypeVariable<?>, InferenceVariable> parameterCaptures = CaptureType
				.capture(
						Types.getAllTypeParameters(Types.getRawType(originalType)),
						parameter -> {
							Type argument = Types.getAllTypeArguments(originalType).get(
									parameter);
							InferenceVariable capturedArgument;

							if (argument instanceof WildcardType) {
								WildcardType wildcardArgument = (WildcardType) argument;

								if (wildcardArgument.getLowerBounds().length > 0) {
									/*
									 * If Ti is a wildcard type argument of the form ? super Bi,
									 * then Si is a fresh type variable whose upper bound is
									 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is Bi.
									 */
									capturedArgument = new InferenceVariable(parameter
											.getBounds(), wildcardArgument.getLowerBounds());
								} else if (wildcardArgument.getUpperBounds().length > 0) {
									/*
									 * If Ti is a wildcard type argument of the form ? extends Bi,
									 * then Si is a fresh type variable whose upper bound is
									 * glb(Bi, Ui[A1:=S1,...,An:=Sn]) and whose lower bound is the
									 * null type.
									 */
									capturedArgument = new InferenceVariable(
											IntersectionType.asArray(IntersectionType.of(
													IntersectionType.uncheckedOf(wildcardArgument
															.getUpperBounds()), IntersectionType
															.uncheckedOf(parameter.getBounds()))),
											new Type[0]);
								} else {
									/*
									 * If Ti is a wildcard type argument (§4.5.1) of the form ?,
									 * then Si is a fresh type variable whose upper bound is
									 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is the null
									 * type (§4.1).
									 */
									capturedArgument = new InferenceVariable(parameter
											.getBounds(), new Type[0]);
								}
							} else {
								/*
								 * Otherwise, Si = Ti.
								 */
								capturedArgument = new InferenceVariable();
							}

							parameterArguments.put(parameter, argument);
							capturedArguments.put(capturedArgument, argument);
							capturedParameters.put(capturedArgument, parameter);

							return capturedArgument;
						});

		type = (ParameterizedType) Types.parameterizedType(
				Types.getRawType(originalType), parameterCaptures).getType();
		ParameterizedType capturedType = type;

		CaptureConversion captureConversion = new CaptureConversion() {
			@Override
			public ParameterizedType getOriginalType() {
				return originalType;
			}

			@Override
			public Set<InferenceVariable> getInferenceVariables() {
				return capturedArguments.keySet();
			}

			@Override
			public Type getCapturedArgument(InferenceVariable variable) {
				return capturedArguments.get(variable);
			}

			@Override
			public TypeVariable<?> getCapturedParameter(InferenceVariable variable) {
				return capturedParameters.get(variable);
			}

			@Override
			public ParameterizedType getCapturedType() {
				return capturedType;
			}

			@Override
			public String toString() {
				return new StringBuilder().append(getCapturedType().getTypeName())
						.append(" = capture(").append(getOriginalType().getTypeName())
						.append(")").toString();
			}
		};

		return captureConversion;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Set<InferenceVariable> getAllMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				InferenceVariable.class::isInstance);
	}

	public static boolean isProperType(Type type) {
		return getAllMentionedBy(type).isEmpty();
	}
}
