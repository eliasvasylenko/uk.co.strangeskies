package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.utilities.tuples.Pair;

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
		if (Types.isProperType(from) && Types.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false
			 * otherwise.
			 */
			if (!Types.isLooseInvocationContextCompatible(from, to))
				boundConsumer.acceptFalsehood();
		} else if (from != null && TypeLiteral.from(from).isPrimitive())
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying
			 * boxing conversion (§5.1.7) to S. Then the constraint reduces to ‹S' →
			 * T›.
			 */
			new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, TypeLiteral.from(from)
					.wrap().getType(), to).reduceInto(boundConsumer);
		else if (to != null && TypeLiteral.from(to).isPrimitive())
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			new ConstraintFormula(Kind.EQUALITY, from, TypeLiteral.from(to).wrap()
					.getType()).reduceInto(boundConsumer);
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
					&& isUncheckedCompatibleOnly(TypeLiteral.from(from)
							.getComponentType(), TypeLiteral.from(to).getComponentType());
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
		else if (from instanceof InferenceVariable<?>)
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to
			 * the bound α <: T.
			 */
			if (to instanceof InferenceVariable<?>)
				boundConsumer.acceptSubtype((InferenceVariable<?>) from,
						(InferenceVariable<?>) to);
			else
				boundConsumer.acceptSubtype((InferenceVariable<?>) from, to);
		else if (to instanceof InferenceVariable<?>)
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to
			 * the bound S <: α.
			 */
			boundConsumer.acceptSubtype(from, (InferenceVariable<?>) to);
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
				if (!rawType.isAssignableFrom(Types.getRawType(to))) {
					/*
					 * If no such type exists, the constraint reduces to false.
					 */
					boundConsumer.acceptFalsehood();
				} else {
					/*
					 * Otherwise, the constraint reduces to the following new constraints:
					 * for all i (1 ≤ i ≤ n), ‹Bi <= Ai›.
					 */
					List<Pair<Type, Type>> identifiedPairs = new ArrayList<>();
					do {
						for (TypeVariable<?> parameter : rawType.getTypeParameters()) {
							Type toArgument = TypeLiteral.from(to).resolveType(parameter);

							System.out.println("   to " + to + " from " + from);

							System.out.println("     " + to + "[" + parameter + "] = "
									+ toArgument);

							if (toArgument instanceof TypeVariable
									&& ((TypeVariable<?>) toArgument).getGenericDeclaration()
											.equals(rawType)) {
								/*
								 * Again:
								 * 
								 * If no such type exists, the constraint reduces to false.
								 */
								boundConsumer.acceptFalsehood();
								return;
							}
							Type fromArgument = TypeLiteral.from(from).resolveType(parameter);
							System.out.println("     " + from + "[" + parameter + "] = "
									+ fromArgument);

							identifiedPairs.add(new Pair<>(fromArgument, toArgument));
						}
					} while ((rawType = rawType.getEnclosingClass()) != null);

					identifiedPairs.forEach(p -> new ConstraintFormula(Kind.CONTAINMENT,
							p.getLeft(), p.getRight()).reduceInto(boundConsumer));
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
			} else if (to instanceof InferenceVariable<?>) {
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
				} else if (((InferenceVariable<?>) to).getBounds().length > 0) {
					/*
					 * - Otherwise, if T has a lower bound, B, the constraint reduces to
					 * ‹S <: B›.
					 */
					new ConstraintFormula(Kind.SUBTYPE, from,
							IntersectionType.of(((InferenceVariable<?>) to).getBounds()));
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
			} else if (from instanceof InferenceVariable<?>) {
				/*
				 * Otherwise, if S is an inference variable, α, the constraint reduces
				 * to the bound α = T.
				 */
				if (to instanceof InferenceVariable<?>)
					boundConsumer.acceptEquality((InferenceVariable<?>) from,
							(InferenceVariable<?>) to);
				else
					boundConsumer.acceptEquality((InferenceVariable<?>) from, to);
			} else if (to instanceof InferenceVariable<?>) {
				/*
				 * Otherwise, if T is an inference variable, α, the constraint reduces
				 * to the bound S = α.
				 */
				boundConsumer.acceptEquality((InferenceVariable<?>) to, from);
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
