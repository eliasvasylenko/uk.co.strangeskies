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

import uk.co.strangeskies.reflection.BoundSet.IncorporationTarget;

/**
 * <p>
 * A constraint formula, as they are described in chapter 18 of that Java 8
 * language specification.
 * </p>
 * 
 * <p>
 * Roughly, a constraint formula describes an assertion of compatibility between
 * two types, with respect to a particular constraining relationship. This
 * relationship may be reduced into a number of secondary, tertiary, etc.
 * constraint formulae, and then ultimately into a number of bounds, which in
 * turn may be incorporated into a {@link BoundSet}.
 * </p>
 * 
 * @author Elias N Vasylenko
 */
public class ConstraintFormula {
	/**
	 * The kind of a constraint formula describes the nature of the constraint it
	 * represents.
	 * 
	 * @author Elias N Vasylenko
	 */
	public enum Kind {
		/**
		 * A loose compatibility constraint implies that two types be compatible
		 * within a loose invocation context, as described by
		 * {@link Types#isLooseInvocationContextCompatible(Type, Type)}.
		 */
		LOOSE_COMPATIBILILTY,
		/**
		 * A subtype constraint between two types implies that the first be
		 * assignable to the second.
		 */
		SUBTYPE,
		/**
		 * A containment constraint between two types implies that one contains the
		 * other, as described by {@link Types#isContainedBy(Type, Type)}.
		 */
		CONTAINMENT,
		/**
		 * An equality constraint between two types implies that they are exactly
		 * identical.
		 */
		EQUALITY
	}

	private final Kind kind;
	private final Type from, to;

	private ConstraintFormula(Kind kind, Type from, Type to) {
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString() {
		return kind + " between '" + from + "' and '" + to + "'";
	}

	/**
	 * Creates a {@link ConstraintFormula} and reduces it into the given
	 * {@link BoundSet}.
	 * 
	 * @param kind
	 *          The kind of the constraint formula to create.
	 * @param from
	 *          The first type of the constraint formula.
	 * @param to
	 *          The second type of the constraint formula.
	 * @param bounds
	 *          The bound set to reduce the created constraint formula into.
	 * @return The constraint formula created.
	 */
	public static ConstraintFormula reduce(Kind kind, Type from, Type to,
			BoundSet bounds) {
		ConstraintFormula constraintFormula = new ConstraintFormula(kind, from, to);
		try {
			constraintFormula.reduceInto(bounds);
		} catch (Exception e) {
			throw new TypeInferenceException("Cannot reduce constraint '"
					+ constraintFormula + "' into bound set '" + bounds + "'.", e);
		}
		return constraintFormula;
	}

	void reduceInto(BoundSet bounds) {
		logConstraint(this, bounds);

		switch (kind) {
		case LOOSE_COMPATIBILILTY:
			reduceLooseCompatibilityConstraint(bounds);
			break;
		case SUBTYPE:
			reduceSubtypeConstraint(bounds);
			break;
		case CONTAINMENT:
			reduceContainmentConstraint(bounds);
			break;
		case EQUALITY:
			reduceEqualityConstraint(bounds);
			break;
		default:
			throw new AssertionError();
		}
	}

	private void logConstraint(ConstraintFormula constraintFormula,
			BoundSet boundSet) {
		// System.out.println(constraintFormula + " into '" + boundSet + "'.");
	}

	/*
	 * A constraint formula of the form ‹S → T› is reduced as follows:
	 */
	private void reduceLooseCompatibilityConstraint(BoundSet bounds) {
		IncorporationTarget incorporate = bounds.incorporate();

		Type from = InferenceVariable.captureConversion(this.from, bounds);

		if (bounds.isProperType(from) && bounds.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false
			 * otherwise.
			 */
			if (!Types.isLooseInvocationContextCompatible(from, to)) {
				incorporate.falsehood();
			}
		} else if (from != null && Types.isPrimitive(from))
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying
			 * boxing conversion (§5.1.7) to S. Then the constraint reduces to ‹S' →
			 * T›.
			 */
			reduce(Kind.LOOSE_COMPATIBILILTY, Types.wrapPrimitive(from), to, bounds);
		else if (to != null && Types.isPrimitive(to))
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			reduce(Kind.EQUALITY, from, Types.wrapPrimitive(to), bounds);
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
			reduce(Kind.SUBTYPE, from, to, bounds);
	}

	private static boolean isUncheckedCompatibleOnly(Type from, Type to) {
		Class<?> toRaw = Types.getRawType(to);
		Class<?> fromRaw = Types.getRawType(from);

		if (to instanceof ParameterizedType) {
			return (toRaw.getTypeParameters().length > 0)
					&& (toRaw.isAssignableFrom(fromRaw))
					&& (TypeToken.of(from).resolveSupertypeParameters(toRaw).getType() instanceof Class);
		} else
			return toRaw.isArray()
					&& fromRaw.isArray()
					&& isUncheckedCompatibleOnly(Types.getComponentType(from),
							Types.getComponentType(to));
	}

	/*
	 * A constraint formula of the form ‹S <: T› is reduced as follows:
	 */
	private void reduceSubtypeConstraint(BoundSet bounds) {
		IncorporationTarget incorporate = bounds.incorporate();

		if (bounds.isProperType(from) && bounds.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is a
			 * subtype of T (§4.10), and false otherwise.
			 */
			if (!Types.isAssignable(from, to) && !(from instanceof InferenceVariable)
					&& !(to instanceof InferenceVariable)) {
				incorporate.falsehood();
			} else
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
			incorporate.falsehood();
		else if (bounds.getInferenceVariables().contains(from))
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to
			 * the bound α <: T.
			 */
			incorporate.subtype(from, to);
		else if (bounds.getInferenceVariables().contains(to))
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to
			 * the bound S <: α.
			 */
			incorporate.subtype(from, to);
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
				if (!Types.getRawTypes(from).stream()
						.anyMatch(t -> rawType.isAssignableFrom(t))) {
					/*
					 * If no such type exists, the constraint reduces to false.
					 */
					if (!(from instanceof InferenceVariable))
						incorporate.falsehood();
				} else {
					Map<TypeVariable<?>, Type> toArguments = ParameterizedTypes
							.getAllTypeArguments((ParameterizedType) to);

					List<Type> fromSet;
					if (this.from instanceof WildcardType)
						fromSet = Arrays.asList(((WildcardType) from).getUpperBounds()[0]);
					else if (this.from instanceof IntersectionType)
						fromSet = Arrays.asList(((IntersectionType) from).getTypes());
					else
						fromSet = Arrays.asList(from);

					for (Type from : fromSet) {
						if (rawType.isAssignableFrom(Types.getRawType(from))
								&& from instanceof ParameterizedType) {

							ParameterizedType fromParameterization = (ParameterizedType) ParameterizedTypes
									.resolveSupertypeParameters(from, rawType);
							if (!(fromParameterization instanceof ParameterizedType))
								/*
								 * If no such type exists, the constraint reduces to false.
								 */
								incorporate.falsehood();

							/*
							 * Otherwise, the constraint reduces to the following new
							 * constraints: for all i (1 ≤ i ≤ n), ‹Bi <= Ai›.
							 */
							ParameterizedTypes
									.getAllTypeArguments(fromParameterization)
									.entrySet()
									.forEach(
											e -> {
												reduce(Kind.CONTAINMENT, e.getValue(),
														toArguments.get(e.getKey()), bounds);
											});
						}
					}
				}
			} else if (to instanceof Class) {
				/*
				 * If T is any other class or interface type, then the constraint
				 * reduces to true if T is among the supertypes of S, and false
				 * otherwise.
				 */
				Type from = this.from;
				if (bounds.getInferenceVariables().contains(from))
					from = IntersectionType.uncheckedFrom(bounds.getBoundsOn(
							(InferenceVariable) from).getUpperBounds());
				if (!Types.isAssignable(from, to))
					incorporate.falsehood();
			} else if (!(to instanceof IntersectionType)
					&& Types.getRawType(to).isArray()) {
				/*
				 * If T is an array type, T'[], then among the supertypes of S that are
				 * array types, a most specific type is identified, S'[] (this may be S
				 * itself).
				 */
				TypeToken<?> fromComponent;
				if ((fromComponent = findMostSpecificArrayType(from)) == null) {
					/*
					 * If no such array type exists, the constraint reduces to false.
					 */
					incorporate.falsehood();
				} else {
					/*
					 * Otherwise:
					 */
					TypeToken<?> toComponent = TypeToken.of(Types.getComponentType(to));
					if (!fromComponent.isPrimitive() && !toComponent.isPrimitive()) {
						/*
						 * - If neither S' nor T' is a primitive type, the constraint
						 * reduces to ‹S' <: T'›.
						 */
						reduce(Kind.SUBTYPE, fromComponent.getType(),
								toComponent.getType(), bounds);
					} else {
						/*
						 * - Otherwise, the constraint reduces to true if S' and T' are the
						 * same primitive type, and false otherwise.
						 */
						if (!fromComponent.isPrimitive()
								|| !fromComponent.equals(toComponent))
							incorporate.falsehood();
					}
				}
			} else if (to instanceof TypeVariableCapture) {
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
				} else if (((TypeVariableCapture) to).getLowerBounds().length > 0) {
					/*
					 * - Otherwise, if T has a lower bound, B, the constraint reduces to
					 * ‹S <: B›.
					 */
					reduce(
							Kind.SUBTYPE,
							from,
							IntersectionType.from(((TypeVariableCapture) to).getLowerBounds()),
							bounds);
				} else {
					/*
					 * - Otherwise, the constraint reduces to false.
					 */
					incorporate.falsehood();
				}
			} else if (to instanceof IntersectionType) {
				/*
				 * If T is an intersection type, I1 & ... & In, the constraint reduces
				 * to the following new constraints: for all i (1 ≤ i ≤ n), ‹S <: Ii›.
				 */
				for (Type typeComponent : ((IntersectionType) to).getTypes())
					reduce(Kind.SUBTYPE, from, typeComponent, bounds);
			} else {
				throw new AssertionError("Type '" + to
						+ "' of T should not be encountered in constraint '" + this + "'.");
			}
		}
	}

	private TypeToken<?> findMostSpecificArrayType(Type from) {
		TypeToken<?> fromToken = TypeToken.of(from);

		if (fromToken.getRawType().isArray()) {
			return TypeToken.of(Types.getComponentType(from));
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
				return TypeToken.of(mostSpecific);
		}

		return null;
	}

	/*
	 * A constraint formula of the form ‹S <= T›, where S and T are type arguments
	 * (§4.5.1), is reduced as follows:
	 */
	private void reduceContainmentConstraint(BoundSet bounds) {
		IncorporationTarget incorporate = bounds.incorporate();

		if (!(to instanceof WildcardType)) {
			/*
			 * If T is a type:
			 */
			if (!(from instanceof WildcardType)) {
				/*
				 * If S is a type, the constraint reduces to ‹S = T›.
				 */
				reduce(Kind.EQUALITY, from, to, bounds);
			} else {
				/*
				 * If S is a wildcard, the constraint reduces to false.
				 */
				incorporate.falsehood();
			}
		} else {
			WildcardType toWildcard = (WildcardType) to;

			if (toWildcard.getLowerBounds().length == 0) {
				if (toWildcard.getUpperBounds().length == 0) {
					/*
					 * If T is a wildcard of the form ?, the constraint reduces to true.
					 */
					return;
				} else {
					/*
					 * If T is a wildcard of the form ? extends T':
					 */
					Type intersectionT = IntersectionType.from(toWildcard
							.getUpperBounds());

					if (!(from instanceof WildcardType)) {
						/*
						 * If S is a type, the constraint reduces to ‹S <: T'›.
						 */
						reduce(Kind.SUBTYPE, from, intersectionT, bounds);
					} else {
						WildcardType from = (WildcardType) this.from;

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * If S is a wildcard of the form ?, the constraint reduces to
								 * ‹Object <: T'›.
								 */
								reduce(Kind.SUBTYPE, Object.class, intersectionT, bounds);
							} else {
								/*
								 * If S is a wildcard of the form ? extends S', the constraint
								 * reduces to ‹S' <: T'›.
								 */
								reduce(Kind.SUBTYPE,
										IntersectionType.from(from.getUpperBounds()),
										intersectionT, bounds);
							}
						} else {
							/*
							 * If S is a wildcard of the form ? super S', the constraint
							 * reduces to ‹Object = T'›.
							 */
							reduce(Kind.EQUALITY, Object.class, intersectionT, bounds);
						}
					}
				}
			} else {
				/*
				 * If T is a wildcard of the form ? super T':
				 */
				Type intersectionT = IntersectionType.from(toWildcard.getLowerBounds());

				if (!(from instanceof WildcardType)) {
					/*
					 * If S is a type, the constraint reduces to ‹T' <: S›.
					 */
					reduce(Kind.SUBTYPE, intersectionT, from, bounds);
				} else {
					WildcardType from = (WildcardType) this.from;

					if (from.getLowerBounds().length > 0) {
						/*
						 * If S is a wildcard of the form ? super S', the constraint reduces
						 * to ‹T' <: S'›.
						 */
						reduce(Kind.SUBTYPE, intersectionT,
								IntersectionType.from(from.getLowerBounds()), bounds);
					} else {
						/*
						 * Otherwise, the constraint reduces to false.
						 */
						incorporate.falsehood();
					}
				}
			}
		}
	}

	private void reduceEqualityConstraint(BoundSet bounds) {
		IncorporationTarget incorporate = bounds.incorporate();

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
									IntersectionType.from(to.getUpperBounds()), bounds);
						}
					}
				} else if (to.getLowerBounds().length == 0) {
					if (to.getUpperBounds().length == 0) {
						/*
						 * If S has the form ? extends S' and T has the form ?, the
						 * constraint reduces to ‹S' = Object›.
						 */
						reduce(Kind.EQUALITY, IntersectionType.from(from.getUpperBounds()),
								Object.class, bounds);
					} else {
						/*
						 * If S has the form ? extends S' and T has the form ? extends T',
						 * the constraint reduces to ‹S' = T'›.
						 */
						reduce(Kind.EQUALITY, IntersectionType.from(from.getUpperBounds()),
								IntersectionType.from(to.getUpperBounds()), bounds);
					}
				}
			} else if (to.getLowerBounds().length > 0) {
				/*
				 * If S has the form ? super S' and T has the form ? super T', the
				 * constraint reduces to ‹S' = T'›.
				 */
				reduce(Kind.EQUALITY, IntersectionType.from(from.getLowerBounds()),
						IntersectionType.from(to.getLowerBounds()), bounds);
			} else {
				/*
				 * Otherwise, the constraint reduces to false.
				 */
				incorporate.falsehood();
			}
		} else {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are types, is
			 * reduced as follows:
			 */
			if (bounds.isProperType(from) && bounds.isProperType(to)) {
				/*
				 * If S and T are proper types, the constraint reduces to true if S is
				 * the same as T (§4.3.4), and false otherwise.
				 */
				if (!from.equals(to))
					incorporate.falsehood();
			} else if (bounds.getInferenceVariables().contains(from)) {
				/*
				 * Otherwise, if S is an inference variable, α, the constraint reduces
				 * to the bound α = T.
				 */
				incorporate.equality(from, to);
			} else if (bounds.getInferenceVariables().contains(to)) {
				/*
				 * Otherwise, if T is an inference variable, α, the constraint reduces
				 * to the bound S = α.
				 */
				incorporate.equality(from, to);
			} else if (Types.getRawType(from).isArray()
					&& Types.getRawType(to).isArray()) {
				/*
				 * Otherwise, if S and T are array types, S'[] and T'[], the constraint
				 * reduces to ‹S' = T'›.
				 */
				reduce(Kind.EQUALITY, Types.getComponentType(from),
						Types.getComponentType(to), bounds);
			} else if (Types.getRawType(from).equals(Types.getRawType(to))) {
				/*
				 * Otherwise, if S and T are class or interface types with the same
				 * erasure, where S has type arguments B1, ..., Bn and T has type
				 * arguments A1, ..., An, the constraint reduces to the following new
				 * constraints: for all i (1 ≤ i ≤ n), ‹Bi = Ai›.
				 */
				ParameterizedTypes.getAllTypeParameters(Types.getRawType(from))
						.forEach(
								type -> reduce(Kind.EQUALITY, TypeToken.of(from)
										.getTypeArgument(type),
										TypeToken.of(to).getTypeArgument(type), bounds));
			}
		}
	}
}
