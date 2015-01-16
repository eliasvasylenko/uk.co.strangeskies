package uk.co.strangeskies.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
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

						@Override
						public void acceptCaptureConversion(CaptureConversion c2) {
							incorporator.incorporateCaptureSubtype(c2, a, b);
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
						public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
							incorporator.incorporateTransitiveSubtype(a, b, a2, b2);
							incorporator.incorporateTransitiveSubtype(a, b, b2, a2);
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
						incorporate(new ConstraintFormula(Kind.SUBTYPE,
								IntersectionType.of(A.getUpperBounds()), R));
					}

					bounds = A.getUpperBounds();
					if (bounds.length == 0
							|| (bounds.length == 1 && bounds[0].equals(Object.class))) {
						/*
						 * If T is Object, then αi <: R implies the constraint formula ‹Bi θ
						 * <: R›
						 */
						// incorporate(new ConstraintFormula(Kind.SUBTYPE,
						// IntersectionType.of(A.getUpperBounds()), R));

						System.out.println(BoundSet.this);
						throw new UnsupportedOperationException();
					}
				} else if (A.getLowerBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? super T:
					 *
					 * αi <: R implies the constraint formula ‹Bi θ <: R›
					 */
					constraints.add(new ConstraintFormula(Kind.SUBTYPE, IntersectionType
							.of(a.getLowerBounds()), R));
				} else {
					/*
					 * If Ai is a wildcard of the form ?:
					 *
					 * αi <: R implies the constraint formula ‹Bi θ <: R›
					 */
					System.out.println(BoundSet.this);
					throw new UnsupportedOperationException();
				}
			}
		}

		public void incorporateCaptureSubtype(CaptureConversion c, Type R,
				InferenceVariable a) {
			if (c.getCapturedArgument(a) instanceof WildcardType) {
				WildcardType A = (WildcardType) c.getCapturedArgument(a);

				if (A.getUpperBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? extends T:
					 *
					 * R <: αi implies the bound false
					 */
					incorporate().acceptFalsehood();
				} else if (A.getLowerBounds().length > 0) {
					/*
					 * If Ai is a wildcard of the form ? super T:
					 *
					 * R <: αi implies the constraint formula ‹R <: T›
					 */
					System.out.println(BoundSet.this);
					throw new UnsupportedOperationException();
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
}
