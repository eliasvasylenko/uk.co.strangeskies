package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class BoundSet {
	private final Set<Bound> bounds;

	public BoundSet() {
		bounds = new HashSet<>();
	}

	public BoundSet(BoundSet bounds) {
		this.bounds = new HashSet<>(bounds.bounds);
	}

	@Override
	public String toString() {
		return new StringBuilder("{ ")
				.append(
						bounds.stream().map(Object::toString)
								.collect(Collectors.joining(", "))).append(" } ").toString();
	}

	public Stream<Bound> stream() {
		return bounds.stream();
	}

	public BoundVisitor incorporate() {
		return new ReductionTarget();
	}

	public void incorporate(ConstraintFormula constraintFormula) {
		constraintFormula.reduceInto(new ReductionTarget(constraintFormula));
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

				incorporator.constraints.forEach(BoundSet.this::incorporate);

				return true;
			} else
				return false;
		}

		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {
			addAndCheckPairs(new Bound(visitor -> visitor.acceptEquality(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateTransitiveEquality(a, b, a2, b2);
							incorporator.incorporateTransitiveEquality(a, b, b2, a2);
							incorporator.incorporateTransitiveEquality(b, a, a2, b2);
							incorporator.incorporateTransitiveEquality(b, a, b2, a2);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateTransitiveEquality(a, b, a2, b2);
							incorporator.incorporateTransitiveEquality(b, a, a2, b2);
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

						@Override
						public void acceptCaptureConversion(
								Map<Type, InferenceVariable> c2) {
							throw new NotImplementedException(); // TODO
						}
					});
		}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {
			addAndCheckPairs(new Bound(visitor -> visitor.acceptEquality(a, b)),
					incorporator -> new PartialBoundVisitor() {
						@Override
						public void acceptEquality(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateTransitiveEquality(a, b, a2, b2);
							incorporator.incorporateTransitiveEquality(a, b, b2, a2);
						}

						@Override
						public void acceptEquality(InferenceVariable a2, Type b2) {
							incorporator.incorporateTransitiveEquality(a, b, a2, b2);

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
						public void acceptSubtype(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateSubtypeSubstitution(a, b, a2, b2);
							incorporator.incorporateSupertypeSubstitution(a, b, a2, b2);
							incorporator.incorporateProperSubtypeSubstitution(a, b, a2, b2);
						}
					});
		}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			addAndCheckPairs(new Bound(visitor -> visitor.acceptSubtype(a, b)),
					incorporator -> new PartialBoundVisitor() {});
			throw new NotImplementedException(); // TODO
		}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {
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
						public void acceptSubtype(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateSupertypeParameterizationEquality(a, b,
									a2, b2);

							incorporator.incorporateTransitiveSubtype(a2, b2, a, b);
							incorporator.incorporateTransitiveSubtype(b2, a2, a, b);
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
					});
		}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {
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
						public void acceptSubtype(InferenceVariable a2,
								InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
							incorporator.incorporateTransitiveSubtype(a, b, b2, a2);
						}

						@Override
						public void acceptSubtype(InferenceVariable a2, Type b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
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

		@Override
		public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {
			bounds.add(new Bound(visitor -> visitor.acceptCaptureConversion(c)));
		}
	}

	/*
	 * (In this section, S and T are inference variables or types, and U is a
	 * proper type. For conciseness, a bound of the form α = T may also match a
	 * bound of the form T = α.)
	 * 
	 * When a bound set contains a pair of bounds that match one of the following
	 * rules, a new constraint formula is implied:
	 */
	class ComplementaryBoundIncorporator {
		private final List<ConstraintFormula> constraints = new ArrayList<>();

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
		public void incorporateSupertypeSubstitution(InferenceVariable a,
				Type S, Type T, InferenceVariable a2) {
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
			if (Types.isProperType(U)) {
				TypeSubstitution resolver = new TypeSubstitution().where(a, U);

				if (!(S instanceof InferenceVariable) && !Types.isProperType(S))
					S = resolver.resolve(S);
				if (!(T instanceof InferenceVariable) && !Types.isProperType(T))
					T = resolver.resolve(T);

				constraints.add(new ConstraintFormula(Kind.EQUALITY, S, T));
			}
		}

		/*
		 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
		 */
		public void incorporateProperSubtypeSubstitution(InferenceVariable a,
				Type U, Type S, Type T) {
			if (Types.isProperType(U)) {
				TypeSubstitution resolver = new TypeSubstitution().where(a, U);

				if (!(S instanceof InferenceVariable) && !Types.isProperType(S))
					S = resolver.resolve(S);
				if (!(T instanceof InferenceVariable) && !Types.isProperType(T))
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
				TypeLiteral<?> literalS = TypeLiteral.from(S);
				TypeLiteral<?> literalT = TypeLiteral.from(T);

				new TypeVisitor() {
					@Override
					protected void visitClass(Class<?> type) {
						visit(type.getGenericInterfaces());
						visit(type.getGenericSuperclass());
					}

					@Override
					protected void visitParameterizedType(ParameterizedType type) {
						Class<?> rawClass = (Class<?>) type.getRawType();

						if (rawClass.isAssignableFrom(literalT.getRawType())) {
							do {
								Type supertypeS = literalS.resolveSupertypeParameters(rawClass)
										.getType();
								Type supertypeT = literalT.resolveSupertypeParameters(rawClass)
										.getType();

								for (int i = 0; i < rawClass.getTypeParameters().length; i++) {
									Type argumentS = ((ParameterizedType) supertypeS)
											.getActualTypeArguments()[i];
									Type argumentT = ((ParameterizedType) supertypeT)
											.getActualTypeArguments()[i];

									if (!(argumentS instanceof WildcardType)
											&& !(argumentT instanceof WildcardType)) {
										constraints.add(new ConstraintFormula(Kind.EQUALITY,
												argumentS, argumentT));
									}
								}
							} while ((rawClass = rawClass.getEnclosingClass()) != null);
						} else
							visitClass(rawClass);
					}
				}.visit(S);
			}
		}
	}
}
