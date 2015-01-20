/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

	private static void reduce(Kind kind, Type from, Type to,
			BoundVisitor boundConsumer) {
		new ConstraintFormula(kind, from, to).reduceInto(boundConsumer);
	}

	void reduceInto(BoundVisitor boundConsumer) {
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
			reduce(Kind.LOOSE_COMPATIBILILTY, Types.wrap(from), to, boundConsumer);
		else if (to != null && Types.isPrimitive(to))
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			reduce(Kind.EQUALITY, from, Types.wrap(to), boundConsumer);
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
			reduce(Kind.SUBTYPE, from, to, boundConsumer);
	}

	/*
	 * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type
	 * parameters A1,...,An with corresponding bounds U1,...,Un.
	 */
	private Type captureConversion(Type type, BoundVisitor boundConsumer) {
		if (type instanceof ParameterizedType) {
			Map<TypeVariable<?>, Type> typeArguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) type);

			if (typeArguments.values().stream()
					.anyMatch(WildcardType.class::isInstance)) {
				CaptureConversion captureConversion = InferenceVariable
						.capture((ParameterizedType) type);

				for (InferenceVariable inferenceVariable : captureConversion
						.getInferenceVariables()) {
					boolean anyProper = false;
					boolean anyBounds = false;
					for (Type bound : inferenceVariable.getUpperBounds()) {
						anyBounds = true;
						anyProper = anyProper || Types.isProperType(bound);
						boundConsumer.acceptSubtype(inferenceVariable, bound);
					}
					if (!anyProper)
						boundConsumer.acceptSubtype(inferenceVariable, Object.class);

					for (Type bound : inferenceVariable.getLowerBounds()) {
						anyBounds = true;
						boundConsumer.acceptSubtype(bound, inferenceVariable);
					}

					if (!anyBounds)
						boundConsumer.acceptEquality(inferenceVariable,
								captureConversion.getCapturedArgument(inferenceVariable));
				}

				boundConsumer.acceptCaptureConversion(captureConversion);

				return captureConversion.getCapturedType();
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
								reduce(Kind.CONTAINMENT,
										fromParameterization.getTypeArgument(p),
										toLiteral.getTypeArgument(p), boundConsumer);
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
						reduce(Kind.SUBTYPE, fromComponent.getType(),
								toComponent.getType(), boundConsumer);
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
					reduce(Kind.SUBTYPE, from,
							IntersectionType.from(((InferenceVariable) to).getLowerBounds()),
							boundConsumer);
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
					reduce(Kind.SUBTYPE, from, typeComponent, boundConsumer);
			} else {
				throw new AssertionError("Type '" + to
						+ "' of T should not be encountered in constraint '" + this + "'.");
			}
		}
	}

	private TypeLiteral<?> findMostSpecificArrayType(Type from) {
		TypeLiteral<?> fromToken = TypeLiteral.from(from);

		if (fromToken.getRawType().isArray()) {
			return TypeLiteral.from(Types.getComponentType(from));
		}

		if (from instanceof WildcardType) {
			from = IntersectionType.from(((WildcardType) from).getUpperBounds());
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
				reduce(Kind.EQUALITY, from, to, boundConsumer);
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
					Type intersectionT = IntersectionType.from(to.getUpperBounds());

					if (!(from instanceof WildcardType)) {
						/*
						 * If S is a type, the constraint reduces to ‹S <: T'›.
						 */
						reduce(Kind.SUBTYPE, from, intersectionT, boundConsumer);
					} else {
						WildcardType from = (WildcardType) this.from;

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * If S is a wildcard of the form ?, the constraint reduces to
								 * ‹Object <: T'›.
								 */
								reduce(Kind.SUBTYPE, Object.class, intersectionT, boundConsumer);
							} else {
								/*
								 * If S is a wildcard of the form ? extends S', the constraint
								 * reduces to ‹S' <: T'›.
								 */
								reduce(Kind.SUBTYPE,
										IntersectionType.from(from.getUpperBounds()),
										intersectionT, boundConsumer);
							}
						} else {
							/*
							 * If S is a wildcard of the form ? super S', the constraint
							 * reduces to ‹Object = T'›.
							 */
							reduce(Kind.EQUALITY, Object.class, intersectionT, boundConsumer);
						}
					}
				}
			} else {
				/*
				 * If T is a wildcard of the form ? super T':
				 */
				Type intersectionT = IntersectionType.from(to.getLowerBounds());

				if (!(from instanceof WildcardType)) {
					/*
					 * If S is a type, the constraint reduces to ‹T' <: S›.
					 */
					reduce(Kind.SUBTYPE, intersectionT, from, boundConsumer);
				} else {
					WildcardType from = (WildcardType) this.from;

					if (from.getLowerBounds().length > 0) {
						/*
						 * If S is a wildcard of the form ? super S', the constraint reduces
						 * to ‹T' <: S'›.
						 */
						reduce(Kind.SUBTYPE, intersectionT,
								IntersectionType.from(from.getLowerBounds()), boundConsumer);
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
							reduce(Kind.EQUALITY, Object.class,
									IntersectionType.from(to.getUpperBounds()), boundConsumer);
						}
					}
				} else if (to.getLowerBounds().length == 0) {
					if (to.getUpperBounds().length == 0) {
						/*
						 * If S has the form ? extends S' and T has the form ?, the
						 * constraint reduces to ‹S' = Object›.
						 */
						reduce(Kind.EQUALITY, IntersectionType.from(from.getUpperBounds()),
								Object.class, boundConsumer);
					} else {
						/*
						 * If S has the form ? extends S' and T has the form ? extends T',
						 * the constraint reduces to ‹S' = T'›.
						 */
						reduce(Kind.EQUALITY, IntersectionType.from(from.getUpperBounds()),
								IntersectionType.from(to.getUpperBounds()), boundConsumer);
					}
				}
			} else if (to.getLowerBounds().length > 0) {
				/*
				 * If S has the form ? super S' and T has the form ? super T', the
				 * constraint reduces to ‹S' = T'›.
				 */
				reduce(Kind.EQUALITY, IntersectionType.from(from.getLowerBounds()),
						IntersectionType.from(to.getLowerBounds()), boundConsumer);
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
				reduce(Kind.EQUALITY, Types.getComponentType(from),
						Types.getComponentType(to), boundConsumer);
			} else if (Types.getRawType(from).equals(Types.getRawType(to))) {
				/*
				 * Otherwise, if S and T are class or interface types with the same
				 * erasure, where S has type arguments B1, ..., Bn and T has type
				 * arguments A1, ..., An, the constraint reduces to the following new
				 * constraints: for all i (1 ≤ i ≤ n), ‹Bi = Ai›.
				 */
				ParameterizedTypes.getAllTypeParameters(Types.getRawType(from))
						.forEach(
								type -> reduce(Kind.EQUALITY, TypeLiteral.from(from)
										.getTypeArgument(type), TypeLiteral.from(to)
										.getTypeArgument(type), boundConsumer));
			}
		}
	}
}
