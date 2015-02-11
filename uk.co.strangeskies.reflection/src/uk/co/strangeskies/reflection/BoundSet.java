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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class BoundSet {
	private static final AtomicLong COUNTER = new AtomicLong();

	private class InferenceVariableData {
		private final InferenceVariable inferenceVariable;
		private final Set<Type> upperBounds;
		private final Set<Type> lowerBounds;
		private final Set<Type> equalities;

		public InferenceVariableData(InferenceVariable inferenceVariable) {
			this.inferenceVariable = inferenceVariable;
			upperBounds = new HashSet<>();
			lowerBounds = new HashSet<>();
			equalities = new HashSet<>();
		}

		public InferenceVariableData(InferenceVariableData that) {
			inferenceVariable = that.inferenceVariable;
			upperBounds = new HashSet<>(that.upperBounds);
			lowerBounds = new HashSet<>(that.lowerBounds);
			equalities = new HashSet<>(that.equalities);
		}

		public InferenceVariable getInferenceVariable() {
			return inferenceVariable;
		}

		public void addEquality(Type type) {
			equalities.add(type);

			ComplementaryBoundIncorporator incorporator = new ComplementaryBoundIncorporator();
			for (Type equality : equalities) {
				incorporator.incorporateTransitiveEquality(inferenceVariable, type,
						inferenceVariable, equality);
			}
			incorporator.constraints.forEach(c -> c.reduceInto(BoundSet.this));
		}

		public void addEquality(InferenceVariable type) {
			equalities.add(type);

			ComplementaryBoundIncorporator incorporator = new ComplementaryBoundIncorporator();
			for (Type equality : equalities) {
				incorporator.incorporateTransitiveEquality(inferenceVariable, type,
						inferenceVariable, equality);
			}
			incorporator.constraints.forEach(c -> c.reduceInto(BoundSet.this));
		}

		public void addUpperBound(Type type) {
			upperBounds.add(type);
		}

		public void addUpperBound(InferenceVariable type) {
			upperBounds.add(type);
		}

		public void addLowerBound(Type type) {
			lowerBounds.add(type);
		}

		public void addLowerBound(InferenceVariable type) {
			lowerBounds.add(type);
		}
	}

	final Set<Bound> bounds;
	private final Map<InferenceVariable, InferenceVariableData> inferenceVariables;

	public BoundSet() {
		bounds = new HashSet<>();
		inferenceVariables = new HashMap<>();
	}

	public BoundSet(BoundSet boundSet) {
		this();

		bounds.addAll(boundSet.bounds);
		inferenceVariables.putAll(boundSet.inferenceVariables
				.values()
				.stream()
				.collect(
						Collectors.toMap(InferenceVariableData::getInferenceVariable,
								InferenceVariableData::new)));
	}

	public Set<Type> getUpperBounds(InferenceVariable variable) {
		return new HashSet<>(inferenceVariables.get(variable).upperBounds);
	}

	public Set<Type> getLowerBounds(InferenceVariable variable) {
		return new HashSet<>(inferenceVariables.get(variable).lowerBounds);
	}

	public Set<Type> getProperUpperBounds(InferenceVariable variable) {
		return new HashSet<>(inferenceVariables.get(variable).upperBounds.stream()
				.filter(BoundSet.this::isProperType).collect(Collectors.toList()));
	}

	public Set<Type> getProperLowerBounds(InferenceVariable variable) {
		return new HashSet<>(inferenceVariables.get(variable).lowerBounds.stream()
				.filter(BoundSet.this::isProperType).collect(Collectors.toList()));
	}

	public Set<Type> getEqualities(InferenceVariable variable) {
		return new HashSet<>(inferenceVariables.get(variable).equalities);
	}

	public Optional<Type> getInstantiation(InferenceVariable variable) {
		return inferenceVariables.get(variable).equalities.stream()
				.filter(BoundSet.this::isProperType).findAny();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<InferenceVariable> getInferenceVariablesMentionedBy(Type type) {
		return (Set) Types.getAllMentionedBy(type,
				inferenceVariables.keySet()::contains);
	}

	public boolean isProperType(Type type) {
		return getInferenceVariablesMentionedBy(type).isEmpty();
	}

	@Override
	public String toString() {
		return new StringBuilder("{ ")
				.append(
						bounds.stream().map(Object::toString)
								.collect(Collectors.joining(", "))).append(" } ").toString();
	}

	public Set<InferenceVariable> getInferenceVariables() {
		return new HashSet<>(inferenceVariables.keySet());
	}

	public Set<InferenceVariable> getInstantiatedVariables() {
		return inferenceVariables.keySet().stream()
				.filter(i -> getInstantiation(i).isPresent())
				.collect(Collectors.toSet());
	}

	public Stream<Bound> stream() {
		return bounds.stream();
	}

	public BoundVisitor incorporate() {
		return new ReductionTarget();
	}

	class ReductionTarget implements BoundVisitor {
		private final ConstraintFormula constraintFormula;

		public ReductionTarget(ConstraintFormula constraintFormula) {
			this.constraintFormula = constraintFormula;
		}

		public ReductionTarget() {
			this.constraintFormula = null;
		}

		public boolean addAndCheckPairs(Bound bound,
				Function<ComplementaryBoundIncorporator, BoundVisitor> visitor) {
			Set<Bound> bounds = new HashSet<>(BoundSet.this.bounds);

			if (BoundSet.this.bounds.add(bound)) {
				ComplementaryBoundIncorporator incorporator = new ComplementaryBoundIncorporator();

				bounds.forEach(b -> b.accept(visitor.apply(incorporator)));

				incorporator.constraints.forEach(c -> c.reduceInto(BoundSet.this));

				return true;
			} else
				return false;
		}

		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {
			inferenceVariables.get(a).addEquality(b);
			inferenceVariables.get(b).addEquality(a);

			addAndCheckPairs(new Bound(visitor -> visitor.acceptEquality(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							// incorporator.incorporateTransitiveEquality(a, b, a2, b2);
							// incorporator.incorporateTransitiveEquality(a, b, b2, a2);
							// incorporator.incorporateTransitiveEquality(b, a, a2, b2);
							// incorporator.incorporateTransitiveEquality(b, a, b2, a2);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							// incorporator.incorporateTransitiveEquality(a2, b2, a, b);
							// incorporator.incorporateTransitiveEquality(a2, b2, b, a);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateSubtypeSubstitution(a, b, a2, b2);
							incorporator.incorporateSubtypeSubstitution(b, a, a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateSubtypeSubstitution(a, b, a2, b2);
							incorporator.incorporateSubtypeSubstitution(b, a, a2, b2);
						}

						@Override
						public void acceptSubtype(Type a2, InferenceVariable b2) {
							incorporator.incorporateSupertypeSubstitution(a, b, a2, b2);
							incorporator.incorporateSupertypeSubstitution(b, a, a2, b2);
						}
					});
		}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {
			inferenceVariables.get(a).addEquality(b);

			addAndCheckPairs(new Bound(visitor -> visitor.acceptEquality(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							// incorporator.incorporateTransitiveEquality(a, b, a2, b2);
							// incorporator.incorporateTransitiveEquality(a, b, b2, a2);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							// incorporator.incorporateTransitiveEquality(a, b, a2, b2);

							incorporator.incorporateProperEqualitySubstitution(a, b, a2, b2);
							incorporator.incorporateProperEqualitySubstitution(a2, b2, a, b);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateSubtypeSubstitution(a, b, a2, b2);
							incorporator.incorporateProperSubtypeSubstitution(a, b, a2, b2);
						}

						@Override
						public void acceptSubtype(Type a2, InferenceVariable b2) {
							incorporator.incorporateSupertypeSubstitution(a, b, a2, b2);
							incorporator.incorporateProperSubtypeSubstitution(a, b, a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateSubtypeSubstitution(a, b, a2, b2);
							incorporator.incorporateSupertypeSubstitution(a, b, a2, b2);
							incorporator.incorporateProperSubtypeSubstitution(a, b, a2, b2);
						}

						@Override
						public void acceptCaptureConversion(CaptureConversion c2) {
							incorporator.incorporateCaptureEquality(c2, a, b);
						}
					});
		}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			inferenceVariables.get(a).addUpperBound(b);
			inferenceVariables.get(b).addLowerBound(a);

			addAndCheckPairs(new Bound(visitor -> visitor.acceptSubtype(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateSubtypeSubstitution(a2, b2, a, b);
							incorporator.incorporateSubtypeSubstitution(b2, a2, a, b);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateSubtypeSubstitution(a2, b2, a, b);
							incorporator.incorporateSupertypeSubstitution(a2, b2, a, b);
							incorporator.incorporateProperSubtypeSubstitution(a2, b2, a, b);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a2, b2, a, b);
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
							incorporator.incorporateSupertypeParameterizationEquality(a, b,
									a2, b2);
						}

						@Override
						public void acceptSubtype(Type a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a2, b2, a, b);
						}
					});
		}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {
			inferenceVariables.get(a).addUpperBound(b);

			addAndCheckPairs(new Bound(visitor -> visitor.acceptSubtype(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateSubtypeSubstitution(a2, b2, a, b);
							incorporator.incorporateSubtypeSubstitution(b2, a2, a, b);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateSubtypeSubstitution(a2, b2, a, b);
							incorporator.incorporateProperSubtypeSubstitution(a2, b2, a, b);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a2, b2, a, b);
							incorporator.incorporateSupertypeParameterizationEquality(a, b,
									a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateSupertypeParameterizationEquality(a, b,
									a2, b2);
						}

						@Override
						public void acceptSubtype(Type a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a2, b2, a, b);
						}

						@Override
						public void acceptCaptureConversion(CaptureConversion c2) {
							incorporator.incorporateCaptureSubtype(c2, a, b);
						}
					});
		}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {
			inferenceVariables.get(b).addLowerBound(a);

			addAndCheckPairs(new Bound(visitor -> visitor.acceptSubtype(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateSupertypeSubstitution(a2, b2, a, b);
							incorporator.incorporateSupertypeSubstitution(b2, a2, a, b);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateSupertypeSubstitution(a2, b2, a, b);
							incorporator.incorporateProperSubtypeSubstitution(a2, b2, a, b);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
						}

						@Override
						public void acceptCaptureConversion(CaptureConversion c2) {
							incorporator.incorporateCaptureSubtype(c2, a, b);
						}
					});
		}

		@Override
		public void acceptCaptureConversion(CaptureConversion c) {
			addAndCheckPairs(
					new Bound(visitor -> visitor.acceptCaptureConversion(c)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateCaptureEquality(c, a2, b2);
						}

						@Override
						public void acceptSubtype(Type a2, InferenceVariable b2) {
							incorporator.incorporateCaptureSubtype(c, a2, b2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateCaptureSubtype(c, a2, b2);
						}
					});
		}

		@Override
		public void acceptFalsehood() {
			if (constraintFormula != null)
				throw new TypeInferenceException("Cannot reduce constraint "
						+ constraintFormula + " into bounds set " + BoundSet.this + ".");
			else
				throw new TypeInferenceException(
						"Addition of falsehood into bounds set " + BoundSet.this + ".");
		}
	}

	class ComplementaryBoundIncorporator {
		private final List<ConstraintFormula> constraints = new ArrayList<>();

		/*
		 * (In this section, S and T are inference variables or types, and U is a
		 * proper type. For conciseness, a bound of the form α = T may also match a
		 * bound of the form T = α.)
		 * 
		 * When a bound set contains a pair of bounds that match one of the
		 * following rules, a new constraint formula is implied:
		 */

		/*
		 * α = S and α = T imply ‹S = T›
		 */
		public void incorporateTransitiveEquality(InferenceVariable a, Type S,
				InferenceVariable a2, Type T) {
			if (a.equals(a2))
				constraints.add(new ConstraintFormula(Kind.EQUALITY, S, T));
		}

		/*
		 * α = S and α <: T imply ‹S <: T›
		 */
		public void incorporateSubtypeSubstitution(InferenceVariable a, Type S,
				InferenceVariable a2, Type T) {
			if (a.equals(a2))
				constraints.add(new ConstraintFormula(Kind.SUBTYPE, S, T));
		}

		/*
		 * α = S and T <: α imply ‹T <: S›
		 */
		public void incorporateSupertypeSubstitution(InferenceVariable a, Type S,
				Type T, InferenceVariable a2) {
			if (a.equals(a2))
				constraints.add(new ConstraintFormula(Kind.SUBTYPE, T, S));
		}

		/*
		 * S <: α and α <: T imply ‹S <: T›
		 */
		public void incorporateTransitiveSubtype(Type S, InferenceVariable a,
				InferenceVariable a2, Type T) {
			if (a.equals(a2))
				constraints.add(new ConstraintFormula(Kind.SUBTYPE, S, T));
		}

		/*
		 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
		 */
		public void incorporateProperEqualitySubstitution(InferenceVariable a,
				Type U, Type S, Type T) {
			if (BoundSet.this.isProperType(U)) {
				TypeSubstitution resolver = new TypeSubstitution().where(a, U);

				if (!(S instanceof InferenceVariable) && !BoundSet.this.isProperType(S))
					S = resolver.resolve(S);
				if (!(T instanceof InferenceVariable) && !BoundSet.this.isProperType(T))
					T = resolver.resolve(T);

				constraints.add(new ConstraintFormula(Kind.EQUALITY, S, T));
			}
		}

		/*
		 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
		 */
		public void incorporateProperSubtypeSubstitution(InferenceVariable a,
				Type U, Type S, Type T) {
			if (BoundSet.this.isProperType(U)) {
				TypeSubstitution resolver = new TypeSubstitution().where(a, U);

				if (!(S instanceof InferenceVariable) && !BoundSet.this.isProperType(S))
					S = resolver.resolve(S);
				if (!(T instanceof InferenceVariable) && !BoundSet.this.isProperType(T))
					T = resolver.resolve(T);

				constraints.add(new ConstraintFormula(Kind.SUBTYPE, S, T));
			}
		}

		/*
		 * When a bound set contains a pair of bounds α <: S and α <: T, and there
		 * exists a supertype of S of the form G<S1, ..., Sn> and a supertype of T
		 * of the form G<T1, ..., Tn> (for some generic class or interface, G), then
		 * for all i (1 ≤ i ≤ n), if Si and Ti are types (not wildcards), the
		 * constraint formula ‹Si = Ti› is implied.
		 */
		public void incorporateSupertypeParameterizationEquality(
				InferenceVariable a, Type S, InferenceVariable a2, Type T) {
			if (S.equals(T) || S.equals(Object.class) || T.equals(Object.class))
				return;

			if (a.equals(a2)
					&& Types.getRawType(S).isAssignableFrom(Types.getRawType(T))) {
				new TypeVisitor() {
					@Override
					protected void visitClass(Class<?> type) {
						visit(type.getGenericInterfaces());
						visit(type.getGenericSuperclass());
					}

					@Override
					protected void visitParameterizedType(ParameterizedType type) {
						Class<?> rawClass = (Class<?>) type.getRawType();

						if (rawClass.isAssignableFrom(Types.getRawType(T))) {
							Type supertypeS = ParameterizedTypes.resolveSupertypeParameters(
									S, rawClass);
							Type supertypeT = ParameterizedTypes.resolveSupertypeParameters(
									T, rawClass);

							for (TypeVariable<?> parameter : ParameterizedTypes
									.getAllTypeParameters(rawClass)) {
								Type argumentS = ParameterizedTypes.getAllTypeArguments(
										(ParameterizedType) supertypeS).get(parameter);
								Type argumentT = ParameterizedTypes.getAllTypeArguments(
										(ParameterizedType) supertypeT).get(parameter);

								if (!(argumentS instanceof WildcardType)
										&& !(argumentT instanceof WildcardType))
									constraints.add(new ConstraintFormula(Kind.EQUALITY,
											argumentS, argumentT));
							}
						} else
							visitClass(rawClass);
					}
				}.visit(S);
			}
		}

		/*
		 * When a bound set contains a bound of the form G<α1, ..., αn> =
		 * capture(G<A1, ..., An>), new bounds are implied and new constraint
		 * formulas may be implied, as follows.
		 * 
		 * Let P1, ..., Pn represent the type parameters of G and let B1, ..., Bn
		 * represent the bounds of these type parameters. Let θ represent the
		 * substitution [P1:=α1, ..., Pn:=αn]. Let R be a type that is not an
		 * inference variable (but is not necessarily a proper type).
		 * 
		 * A set of bounds on α1, ..., αn is implied, constructed from the declared
		 * bounds of P1, ..., Pn as specified in §18.1.3.
		 * 
		 * In addition, for all i (1 ≤ i ≤ n):
		 */

		public void incorporateCaptureEquality(CaptureConversion c,
				InferenceVariable a, Type R) {
			/*
			 * If Ai is a wildcard of the form ?, or;
			 * 
			 * If Ai is a wildcard of the form ? extends T, or;
			 * 
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * αi = R implies the bound false
			 */
			if (c.getCapturedArgument(a) instanceof WildcardType)
				incorporate().acceptFalsehood();
		}

		public void incorporateCaptureSubtype(CaptureConversion c,
				InferenceVariable a, Type R) {
			if (c.getCapturedArgument(a) instanceof WildcardType) {
				WildcardType A = (WildcardType) c.getCapturedArgument(a);

				TypeSubstitution θ = new TypeSubstitution();
				for (InferenceVariable variable : c.getInferenceVariables())
					θ = θ.where(c.getCapturedParameter(variable), variable);

				if (A.getUpperBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? extends T:
					 */

					Type[] bounds = c.getCapturedParameter(a).getBounds();
					if (bounds.length == 0
							|| (bounds.length == 1 && bounds[0].equals(Object.class))) {
						/*
						 * If Bi is Object, then αi <: R implies the constraint formula ‹T
						 * <: R›
						 */
						constraints.add(new ConstraintFormula(Kind.SUBTYPE,
								IntersectionType.from(A.getUpperBounds()), R));
					}

					bounds = A.getUpperBounds();
					if (bounds.length == 0
							|| (bounds.length == 1 && bounds[0].equals(Object.class))) {
						/*
						 * If T is Object, then αi <: R implies the constraint formula ‹Bi θ
						 * <: R›
						 */
						constraints.add(new ConstraintFormula(Kind.SUBTYPE, θ
								.resolve(IntersectionType.uncheckedFrom(c.getCapturedParameter(
										a).getBounds())), R));
					}
				} else if (A.getLowerBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? super T:
					 * 
					 * αi <: R implies the constraint formula ‹Bi θ <: R›
					 */
					constraints.add(new ConstraintFormula(Kind.SUBTYPE, θ
							.resolve(IntersectionType.uncheckedFrom(c.getCapturedParameter(a)
									.getBounds())), R));
				} else {
					/*
					 * If Ai is a wildcard of the form ?:
					 * 
					 * αi <: R implies the constraint formula ‹Bi θ <: R›
					 */
					constraints.add(new ConstraintFormula(Kind.SUBTYPE, θ
							.resolve(IntersectionType.uncheckedFrom(c.getCapturedParameter(a)
									.getBounds())), R));
				}
			}
		}

		public void incorporateCaptureSubtype(CaptureConversion c, Type R,
				InferenceVariable a) {
			if (c.getCapturedArgument(a) instanceof WildcardType) {
				WildcardType A = (WildcardType) c.getCapturedArgument(a);

				if (A.getLowerBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? super T:
					 * 
					 * R <: αi implies the constraint formula ‹R <: T›
					 */
					constraints.add(new ConstraintFormula(Kind.SUBTYPE, R,
							IntersectionType.uncheckedFrom(A.getLowerBounds())));
				} else if (A.getUpperBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? extends T:
					 * 
					 * R <: αi implies the bound false
					 */
					incorporate().acceptFalsehood();
				} else {
					/*
					 * If Ai is a wildcard of the form ?:
					 * 
					 * R <: αi implies the bound false
					 */
					incorporate().acceptFalsehood();
				}
			}

		}
	}

	public void removeCaptureConversions(
			Collection<? extends CaptureConversion> captureConversions) {
		captureConversions.forEach(c -> bounds.remove(new Bound(b -> b
				.acceptCaptureConversion(c))));
	}

	public InferenceVariable createInferenceVariable() {
		return createInferenceVariable("INF");
	}

	public InferenceVariable createInferenceVariable(String name) {
		String finalName = name + "#" + COUNTER.incrementAndGet();

		InferenceVariable inferenceVariable = new InferenceVariable() {
			@Override
			public String toString() {
				return finalName;
			}
		};
		inferenceVariables.put(inferenceVariable, new InferenceVariableData(
				inferenceVariable));
		return inferenceVariable;
	}
}
