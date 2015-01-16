package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConstraintFormula {
	public enum Kind {
		LOOSE_COMPATIBILILTY, SUBTYPE, CONTAINMENT, EQUALITY
	}

	private final Kind kind;
	private final Type from, to;

	public ConstraintFormula(Kind kind, Type from, Type to) {
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString() {
		return kind + " between '" + from + "' and '" + to + "'";
	}

	public void reduceInto(BoundVisitor boundConsumer) {
		switch (kind) {
		case LOOSE_COMPATIBILILTY:
			reduceLooseCompatibilityConstraint(boundConsumer);
			break;
		case SUBTYPE:
			reduceSubtypeConstraint(boundConsumer);
			break;
		case CONTAINMENT:
			reduceContainmentConstraint(boundConsumer);
			break;
		case EQUALITY:
			reduceEqualityConstraint(boundConsumer);
			break;
		default:
			throw new AssertionError();
		}
	}

	/*
	 * A constraint formula of the form ‹S → T› is reduced as follows:
	 */
	private void reduceLooseCompatibilityConstraint(BoundVisitor boundConsumer) {
		Type from = captureConversion(this.from, boundConsumer);

		if (Types.isProperType(from) && Types.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false
			 * otherwise.
			 */
			if (!Types.isLooseInvocationContextCompatible(from, to))
				boundConsumer.acceptFalsehood();
		} else if (from != null && Types.isPrimitive(from))
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying
			 * boxing conversion (§5.1.7) to S. Then the constraint reduces to ‹S' →
			 * T›.
			 */
			new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, Types.wrap(from), to)
					.reduceInto(boundConsumer);
		else if (to != null && Types.isPrimitive(to))
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			new ConstraintFormula(Kind.EQUALITY, from, Types.wrap(to))
					.reduceInto(boundConsumer);
		else if (isUncheckedCompatibleOnly(from, to))
			/*
			 * Otherwise, if T is a parameterized type of the form G<T1, ..., Tn>, and
			 * there exists no type of the form G<...> that is a supertype of S, but
			 * the raw type G is a supertype of S, then the constraint reduces to
			 * true.
			 *
			 * Otherwise, if T is an array type of the form G<T1, ..., Tn>[]k, and
			 * there exists no type of the form G<...>[]k that is a supertype of S,
			 * but the raw type G[]k is a supertype of S, then the constraint reduces
			 * to true. (The notation []k indicates an array type of k dimensions.)
			 */
			return;
		else
			/*
			 * Otherwise, the constraint reduces to ‹S <: T›.
			 */
			new ConstraintFormula(Kind.SUBTYPE, from, to).reduceInto(boundConsumer);
	}

	/*
	 * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type
	 * parameters A1,...,An with corresponding bounds U1,...,Un.
	 */
	private Type captureConversion(Type type, BoundVisitor boundConsumer) {
		if (type instanceof ParameterizedType) {
			TypeLiteral<?> typeLiteral = TypeLiteral.from(type);
			/*
			 * There exists a capture conversion from a parameterized type
			 * G<T1,...,Tn> (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1
			 * ≤ i ≤ n :
			 */

			Map<TypeVariable<?>, Type> parameterArguments = new HashMap<>();
			Map<TypeVariable<?>, InferenceVariable> parameterCaptures = new HashMap<>();
			Map<InferenceVariable, Type> capturedArguments = new HashMap<>();
			Map<InferenceVariable, TypeVariable<?>> capturedParameters = new HashMap<>();
			boolean identity = true;

			for (TypeVariable<?> parameter : typeLiteral.getTypeParameters()) {
				Type argument = typeLiteral.resolveType(parameter);
				InferenceVariable capturedArgument;

				if (argument instanceof WildcardType) {
					WildcardType wildcardArgument = (WildcardType) argument;
					identity = false;

					if (wildcardArgument.getLowerBounds().length > 0) {
						/*
						 * If Ti is a wildcard type argument of the form ? super Bi, then Si
						 * is a fresh type variable whose upper bound is
						 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is Bi.
						 */
						capturedArgument = new InferenceVariable(parameter.getBounds(),
								wildcardArgument.getUpperBounds());
					} else if (wildcardArgument.getUpperBounds().length > 0) {
						/*
						 * If Ti is a wildcard type argument of the form ? extends Bi, then
						 * Si is a fresh type variable whose upper bound is glb(Bi,
						 * Ui[A1:=S1,...,An:=Sn]) and whose lower bound is the null type.
						 */
						capturedArgument = new InferenceVariable(
								IntersectionType.asArray(IntersectionType.of(IntersectionType
										.uncheckedOf(wildcardArgument.getUpperBounds()),
										IntersectionType.uncheckedOf(parameter.getBounds()))),
								new Type[0]);
					} else {
						/*
						 * If Ti is a wildcard type argument (§4.5.1) of the form ?, then Si
						 * is a fresh type variable whose upper bound is
						 * Ui[A1:=S1,...,An:=Sn] and whose lower bound is the null type
						 * (§4.1).
						 */
						capturedArgument = new InferenceVariable(parameter.getBounds(),
								new Type[0]);
					}
				} else {
					/*
					 * Otherwise, Si = Ti.
					 */
					capturedArgument = new InferenceVariable();
				}

				parameterArguments.put(parameter, argument);
				parameterCaptures.put(parameter, capturedArgument);
				capturedArguments.put(capturedArgument, argument);
				capturedParameters.put(capturedArgument, parameter);
			}

			if (!identity) {
				InferenceVariable.substituteBounds(parameterCaptures);

				ParameterizedType originalType = (ParameterizedType) type;
				type = Types.parameterizedType(Types.getRawType(originalType),
						parameterCaptures).getType();
				ParameterizedType capturedType = (ParameterizedType) type;

				for (Map.Entry<TypeVariable<?>, InferenceVariable> inferenceVariable : parameterCaptures
						.entrySet()) {
					boolean anyProper = false;
					boolean anyBounds = false;
					for (Type bound : inferenceVariable.getValue().getUpperBounds()) {
						anyBounds = true;
						anyProper = anyProper || Types.isProperType(bound);
						boundConsumer.acceptSubtype(inferenceVariable.getValue(), bound);
					}
					if (!anyProper)
						boundConsumer.acceptSubtype(inferenceVariable.getValue(),
								Object.class);

					for (Type bound : inferenceVariable.getValue().getLowerBounds()) {
						anyBounds = true;
						boundConsumer.acceptSubtype(bound, inferenceVariable.getValue());
					}

					if (!anyBounds)
						boundConsumer.acceptEquality(inferenceVariable.getValue(),
								parameterArguments.get(inferenceVariable.getKey()));
				}

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
						return new StringBuilder().append(getCapturedType())
								.append(" = capture(").append(getOriginalType()).append(")")
								.toString();
					}
				};
				System.out.println("new(): " + captureConversion);
				boundConsumer.acceptCaptureConversion(captureConversion);
			}
		}

		return type;
	}

	public static boolean isUncheckedCompatibleOnly(Type from, Type to) {
		Class<?> toRaw = Types.getRawType(to);
		Class<?> fromRaw = Types.getRawType(from);

		if (to instanceof ParameterizedType) {
			return (toRaw.getTypeParameters().length > 0)
					&& (toRaw.isAssignableFrom(fromRaw))
					&& (TypeLiteral.from(from).resolveSupertypeParameters(toRaw)
							.getType() instanceof Class);
		} else
			return toRaw.isArray()
					&& fromRaw.isArray()
					&& isUncheckedCompatibleOnly(Types.getComponentType(from),
							Types.getComponentType(to));
	}

	/*
	 * A constraint formula of the form ‹S <: T› is reduced as follows:
	 */
	private void reduceSubtypeConstraint(BoundVisitor boundConsumer) {
		if (Types.isProperType(from) && Types.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is a
			 * subtype of T (§4.10), and false otherwise.
			 */
			if (!Types.isAssignable(from, to))
				boundConsumer.acceptFalsehood();
			else
				return;
		} else if (from == null)
			/*
			 * Otherwise, if S is the null type, the constraint reduces to true.
			 */
			return;
		else if (to == null)
			/*
			 * Otherwise, if T is the null type, the constraint reduces to false.
			 */
			boundConsumer.acceptFalsehood();
		else if (from instanceof InferenceVariable)
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to
			 * the bound α <: T.
			 */
			if (to instanceof InferenceVariable)
				boundConsumer.acceptSubtype((InferenceVariable) from,
						(InferenceVariable) to);
			else
				boundConsumer.acceptSubtype((InferenceVariable) from, to);
		else if (to instanceof InferenceVariable)
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to
			 * the bound S <: α.
			 */
			boundConsumer.acceptSubtype(from, (InferenceVariable) to);
		else {
			/*
			 * Otherwise, the constraint is reduced according to the form of T:
			 */
			if (to instanceof ParameterizedType) {
				/*
				 * If T is a parameterized class or interface type, or an inner class
				 * type of a parameterized class or interface type (directly or
				 * indirectly), let A1, ..., An be the type arguments of T. Among the
				 * supertypes of S, a corresponding class or interface type is
				 * identified, with type arguments B1, ..., Bn.
				 */
				Class<?> rawType = Types.getRawType(to);
				if (!rawType.isAssignableFrom(Types.getRawType(to)))
					/*
					 * If no such type exists, the constraint reduces to false.
					 */
					boundConsumer.acceptFalsehood();
				else {
					TypeLiteral<?> toLiteral = TypeLiteral.from(to);

					TypeLiteral<?> fromParameterization = TypeLiteral.from(from)
							.resolveSupertypeParameters(rawType);
					if (!(fromParameterization.getType() instanceof ParameterizedType))
						/*
						 * If no such type exists, the constraint reduces to false.
						 */
						boundConsumer.acceptFalsehood();

					/*
					 * Otherwise, the constraint reduces to the following new constraints:
					 * for all i (1 ≤ i ≤ n), ‹Bi <= Ai›.
					 */
					fromParameterization.getTypeParameters().forEach(
							p -> {
								new ConstraintFormula(Kind.CONTAINMENT, fromParameterization
										.resolveType(p), toLiteral.resolveType(p))
										.reduceInto(boundConsumer);
							});
				}
			} else if (to instanceof Class) {
				/*
				 * If T is any other class or interface type, then the constraint
				 * reduces to true if T is among the supertypes of S, and false
				 * otherwise.
				 */
				if (!Types.isAssignable(from, to))
					boundConsumer.acceptFalsehood();
			} else if (!(to instanceof IntersectionType)
					&& Types.getRawType(to).isArray()) {
				/*
				 * If T is an array type, T'[], then among the supertypes of S that are
				 * array types, a most specific type is identified, S'[] (this may be S
				 * itself).
				 */
				TypeLiteral<?> fromComponent;
				if ((fromComponent = findMostSpecificArrayType(from)) == null) {
					/*
					 * If no such array type exists, the constraint reduces to false.
					 */
					boundConsumer.acceptFalsehood();
				} else {
					/*
					 * Otherwise:
					 */
					TypeLiteral<?> toComponent = TypeLiteral.from(Types
							.getComponentType(to));
					if (!fromComponent.isPrimitive() && !toComponent.isPrimitive()) {
						/*
						 * - If neither S' nor T' is a primitive type, the constraint
						 * reduces to ‹S' <: T'›.
						 */
						new ConstraintFormula(Kind.SUBTYPE, fromComponent.getType(),
								toComponent.getType()).reduceInto(boundConsumer);
					} else {
						/*
						 * - Otherwise, the constraint reduces to true if S' and T' are the
						 * same primitive type, and false otherwise.
						 */
						if (!fromComponent.isPrimitive()
								|| !fromComponent.equals(toComponent))
							boundConsumer.acceptFalsehood();
					}
				}
			} else if (to instanceof InferenceVariable) {
				/*
				 * If T is a type variable, there are three cases:
				 */
				if (from instanceof IntersectionType
						&& Arrays.stream(((IntersectionType) from).getTypes()).anyMatch(
								f -> f.equals(to))) {
					/*
					 * - If S is an intersection type of which T is an element, the
					 * constraint reduces to true.
					 */
				} else if (((InferenceVariable) to).getLowerBounds().length > 0) {
					/*
					 * - Otherwise, if T has a lower bound, B, the constraint reduces to
					 * ‹S <: B›.
					 */
					new ConstraintFormula(Kind.SUBTYPE, from,
							IntersectionType.of(((InferenceVariable) to).getLowerBounds()));
				} else {
					/*
					 * - Otherwise, the constraint reduces to false.
					 */
					boundConsumer.acceptFalsehood();
				}
			} else if (to instanceof IntersectionType) {
				/*
				 * If T is an intersection type, I1 & ... & In, the constraint reduces
				 * to the following new constraints: for all i (1 ≤ i ≤ n), ‹S <: Ii›.
				 */
				for (Type typeComponent : ((IntersectionType) to).getTypes())
					new ConstraintFormula(Kind.SUBTYPE, from, typeComponent);
			} else {
				throw new AssertionError("Type '" + to
						+ "' of T should not be encountered here.");
			}
		}
	}

	private TypeLiteral<?> findMostSpecificArrayType(Type from) {
		TypeLiteral<?> fromToken = TypeLiteral.from(from);

		if (fromToken.getRawType().isArray()) {
			return TypeLiteral.from(Types.getComponentType(from));
		}

		if (from instanceof WildcardType) {
			from = IntersectionType.of(((WildcardType) from).getUpperBounds());
		}

		if (from instanceof IntersectionType) {
			List<Type> candidates = Arrays.asList(((IntersectionType) from)
					.getTypes());

			// attempt to find most specific from candidates
			Type mostSpecific = candidates
					.stream()
					.filter(t -> Types.getRawType(t).isArray())
					.reduce(
							(a, b) -> (Types.isAssignable(Types.getComponentType(b),
									Types.getComponentType(a))) ? a : b).orElse(null);

			// verify we really have the most specific
			if (candidates
					.stream()
					.filter(t -> Types.getRawType(t).isArray())
					.anyMatch(
							t -> !Types.isAssignable(Types.getComponentType(mostSpecific),
									Types.getComponentType(t))))
				return TypeLiteral.from(mostSpecific);
		}

		return null;
	}

	/*
	 * A constraint formula of the form ‹S <= T›, where S and T are type arguments
	 * (§4.5.1), is reduced as follows:
	 */
	private void reduceContainmentConstraint(BoundVisitor boundConsumer) {
		if (!(to instanceof WildcardType)) {
			/*
			 * If T is a type:
			 */
			if (!(from instanceof WildcardType)) {
				/*
				 * If S is a type, the constraint reduces to ‹S = T›.
				 */
				new ConstraintFormula(Kind.EQUALITY, from, to)
						.reduceInto(boundConsumer);
			} else {
				/*
				 * If S is a wildcard, the constraint reduces to false.
				 */
				boundConsumer.acceptFalsehood();
			}
		} else {
			WildcardType to = (WildcardType) this.to;

			if (to.getLowerBounds().length == 0) {
				if (to.getUpperBounds().length == 0) {
					/*
					 * If T is a wildcard of the form ?, the constraint reduces to true.
					 */
					return;
				} else {
					/*
					 * If T is a wildcard of the form ? extends T':
					 */
					Type intersectionT = IntersectionType.of(to.getUpperBounds());

					if (!(from instanceof WildcardType)) {
						/*
						 * If S is a type, the constraint reduces to ‹S <: T'›.
						 */
						new ConstraintFormula(Kind.SUBTYPE, from, intersectionT)
								.reduceInto(boundConsumer);
					} else {
						WildcardType from = (WildcardType) this.from;

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * If S is a wildcard of the form ?, the constraint reduces to
								 * ‹Object <: T'›.
								 */
								new ConstraintFormula(Kind.SUBTYPE, Object.class, intersectionT)
										.reduceInto(boundConsumer);
							} else {
								/*
								 * If S is a wildcard of the form ? extends S', the constraint
								 * reduces to ‹S' <: T'›.
								 */
								new ConstraintFormula(Kind.SUBTYPE, IntersectionType.of(from
										.getUpperBounds()), intersectionT)
										.reduceInto(boundConsumer);
							}
						} else {
							/*
							 * If S is a wildcard of the form ? super S', the constraint
							 * reduces to ‹Object = T'›.
							 */
							new ConstraintFormula(Kind.EQUALITY, Object.class, intersectionT)
									.reduceInto(boundConsumer);
						}
					}
				}
			} else {
				/*
				 * If T is a wildcard of the form ? super T':
				 */
				Type intersectionT = IntersectionType.of(to.getLowerBounds());

				if (!(from instanceof WildcardType)) {
					/*
					 * If S is a type, the constraint reduces to ‹T' <: S›.
					 */
					new ConstraintFormula(Kind.SUBTYPE, intersectionT, from);
				} else {
					WildcardType from = (WildcardType) this.from;

					if (from.getLowerBounds().length > 0) {
						/*
						 * If S is a wildcard of the form ? super S', the constraint reduces
						 * to ‹T' <: S'›.
						 */
						new ConstraintFormula(Kind.SUBTYPE, intersectionT,
								IntersectionType.of(from.getLowerBounds()))
								.reduceInto(boundConsumer);
					} else {
						/*
						 * Otherwise, the constraint reduces to false.
						 */
						boundConsumer.acceptFalsehood();
					}
				}
			}
		}
	}

	private void reduceEqualityConstraint(BoundVisitor boundConsumer) {
		if (from instanceof WildcardType && to instanceof WildcardType) {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are type
			 * arguments (§4.5.1), is reduced as follows:
			 */
			WildcardType from = (WildcardType) this.from;
			WildcardType to = (WildcardType) this.to;

			if (from.getLowerBounds().length == 0) {
				if (from.getUpperBounds().length == 0) {
					if (to.getLowerBounds().length == 0) {
						if (to.getUpperBounds().length == 0) {
							/*
							 * If S has the form ? and T has the form ?, the constraint
							 * reduces to true.
							 */
							return;
						} else {
							/*
							 * If S has the form ? and T has the form ? extends T', the
							 * constraint reduces to ‹Object = T'›.
							 */
							new ConstraintFormula(Kind.EQUALITY, Object.class,
									IntersectionType.of(to.getUpperBounds()))
									.reduceInto(boundConsumer);
						}
					}
				} else if (to.getLowerBounds().length == 0) {
					if (to.getUpperBounds().length == 0) {
						/*
						 * If S has the form ? extends S' and T has the form ?, the
						 * constraint reduces to ‹S' = Object›.
						 */
						new ConstraintFormula(Kind.EQUALITY, IntersectionType.of(from
								.getUpperBounds()), Object.class).reduceInto(boundConsumer);
					} else {
						/*
						 * If S has the form ? extends S' and T has the form ? extends T',
						 * the constraint reduces to ‹S' = T'›.
						 */
						new ConstraintFormula(Kind.EQUALITY, IntersectionType.of(from
								.getUpperBounds()), IntersectionType.of(to.getUpperBounds()));
					}
				}
			} else if (to.getLowerBounds().length > 0) {
				/*
				 * If S has the form ? super S' and T has the form ? super T', the
				 * constraint reduces to ‹S' = T'›.
				 */
				new ConstraintFormula(Kind.EQUALITY, IntersectionType.of(from
						.getLowerBounds()), IntersectionType.of(to.getLowerBounds()));
			} else {
				/*
				 * Otherwise, the constraint reduces to false.
				 */
				boundConsumer.acceptFalsehood();
			}
		} else {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are types, is
			 * reduced as follows:
			 */
			if (Types.isProperType(from) && Types.isProperType(to)) {
				/*
				 * If S and T are proper types, the constraint reduces to true if S is
				 * the same as T (§4.3.4), and false otherwise.
				 */
				if (!from.equals(to))
					boundConsumer.acceptFalsehood();
			} else if (from instanceof InferenceVariable) {
				/*
				 * Otherwise, if S is an inference variable, α, the constraint reduces
				 * to the bound α = T.
				 */
				if (to instanceof InferenceVariable)
					boundConsumer.acceptEquality((InferenceVariable) from,
							(InferenceVariable) to);
				else
					boundConsumer.acceptEquality((InferenceVariable) from, to);
			} else if (to instanceof InferenceVariable) {
				/*
				 * Otherwise, if T is an inference variable, α, the constraint reduces
				 * to the bound S = α.
				 */
				boundConsumer.acceptEquality((InferenceVariable) to, from);
			} else if (Types.getRawType(from).isArray()
					&& Types.getRawType(to).isArray()) {
				/*
				 * Otherwise, if S and T are array types, S'[] and T'[], the constraint
				 * reduces to ‹S' = T'›.
				 */
				new ConstraintFormula(Kind.EQUALITY, Types.getComponentType(from),
						Types.getComponentType(to)).reduceInto(boundConsumer);
			} else if (Types.getRawType(from).equals(Types.getRawType(to))) {
				/*
				 * Otherwise, if S and T are class or interface types with the same
				 * erasure, where S has type arguments B1, ..., Bn and T has type
				 * arguments A1, ..., An, the constraint reduces to the following new
				 * constraints: for all i (1 ≤ i ≤ n), ‹Bi = Ai›.
				 */
				Types.getTypeParameters(Types.getRawType(from)).forEach(
						type -> new ConstraintFormula(Kind.EQUALITY, TypeLiteral.from(from)
								.resolveType(type), TypeLiteral.from(to).resolveType(type))
								.reduceInto(boundConsumer));
			}
		}
	}
}
