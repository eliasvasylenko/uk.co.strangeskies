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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

/**
 * This object describes the bounds present on a particular inference variable
 * within the context of a particular bound set.
 * 
 * @author Elias N Vasylenko
 *
 */
public class InferenceVariableBounds {
	private final BoundSet boundSet;

	private final InferenceVariable a;
	private final Set<Type> equalities;
	private final Set<Type> upperBounds;
	private final Set<Type> lowerBounds;

	InferenceVariableBounds(BoundSet boundSet, InferenceVariable inferenceVariable) {
		this.boundSet = boundSet;
		this.a = inferenceVariable;

		upperBounds = new HashSet<>();
		lowerBounds = new HashSet<>();
		equalities = new HashSet<>();
	}

	InferenceVariableBounds(BoundSet boundSet, InferenceVariableBounds that) {
		this.boundSet = boundSet;
		a = that.a;

		upperBounds = new HashSet<>(that.upperBounds);
		lowerBounds = new HashSet<>(that.lowerBounds);
		equalities = new HashSet<>(that.equalities);
	}

	/**
	 * @return The inference variable the bounds described by this object apply
	 *         to.
	 */
	public InferenceVariable getInferenceVariable() {
		return a;
	}

	/**
	 * @return All equality bounds on the described inference variable.
	 */
	public Set<Type> getEqualities() {
		return new HashSet<>(equalities);
	}

	/**
	 * @return All upper bounds on the described inference variable.
	 */
	public Set<Type> getUpperBounds() {
		return new HashSet<>(upperBounds);
	}

	/**
	 * @return All lower bounds on the described inference variable.
	 */
	public Set<Type> getLowerBounds() {
		return new HashSet<>(lowerBounds);
	}

	/**
	 * @return All proper upper bounds on the described inference variable.
	 */
	public Set<Type> getProperUpperBounds() {
		Set<Type> upperBounds = getUpperBounds().stream()
				.filter(boundSet::isProperType).collect(Collectors.toSet());
		return upperBounds.isEmpty() ? new HashSet<>(Arrays.asList(Object.class))
				: upperBounds;
	}

	/**
	 * @return All proper lower bounds on the described inference variable.
	 */
	public Set<Type> getProperLowerBounds() {
		return getLowerBounds().stream().filter(boundSet::isProperType)
				.collect(Collectors.toSet());
	}

	/**
	 * @return All instantiations on the described inference variable.
	 */
	public Optional<Type> getInstantiation() {
		return getEqualities().stream().filter(boundSet::isProperType).findAny();
	}

	void addEquality(Type type) {
		Set<Type> equalities = new HashSet<>(this.equalities);
		if (this.equalities.add(type)) {
			logBound(a, type, "=");

			/*
			 * α = S and α = T imply ‹S = T›
			 */
			for (Type equality : equalities)
				incorporateTransitiveEquality(type, equality);

			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBounds otherBounds = boundSet.getBoundsOn(other);

				/*
				 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
				 */
				for (Type equality : otherBounds.getEqualities())
					if (equality != type)
						incorporateProperEqualitySubstitution(a, type, otherBounds.a,
								equality);

				/*
				 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
				 */
				for (Type supertype : otherBounds.getUpperBounds())
					if (supertype != type)
						incorporateProperSubtypeSubstitution(type, otherBounds.a, supertype);
				for (Type subtype : otherBounds.getLowerBounds())
					if (subtype != type)
						incorporateProperSupertypeSubstitution(type, subtype, otherBounds.a);
			}

			/*
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type supertype : getUpperBounds())
				incorporateSubtypeSubstitution(type, supertype);

			/*
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type subtype : getLowerBounds())
				incorporateSupertypeSubstitution(type, subtype);

			for (CaptureConversion captureConversion : boundSet
					.getCaptureConversions()) {
				Type capturedArgument = captureConversion.getCapturedArgument(a);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedEquality((WildcardType) capturedArgument, type);
			}
		}
	}

	void addEquality(InferenceVariable type) {
		Set<Type> equalities = new HashSet<>(this.equalities);
		if (this.equalities.add(type)) {
			logBound(a, type, "=");

			/*
			 * α = S and α = T imply ‹S = T›
			 */
			for (Type equality : new HashSet<>(equalities))
				incorporateTransitiveEquality(type, equality);

			/*
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type supertype : new HashSet<>(upperBounds))
				incorporateSubtypeSubstitution(type, supertype);

			/*
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type subtype : new HashSet<>(lowerBounds))
				incorporateSupertypeSubstitution(type, subtype);
		}
	}

	void addUpperBound(Type type) {
		Set<Type> upperBounds = new HashSet<>(this.upperBounds);
		if (this.upperBounds.add(type)) {
			logBound(a, type, "<:");

			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBounds otherBounds = boundSet.getBoundsOn(other);

				for (Type equality : new HashSet<>(otherBounds.equalities))
					if (equality != type)
						boundSet.getBoundsOn(otherBounds.a)
								.incorporateProperSubtypeSubstitution(equality, a, type);
			}

			/*
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type equality : new HashSet<>(equalities))
				incorporateSubtypeSubstitution(equality, type);

			/*
			 * S <: α and α <: T imply ‹S <: T›
			 */
			for (Type lowerBound : new HashSet<>(lowerBounds))
				incorporateTransitiveSubtype(lowerBound, type);

			/*
			 * When a bound set contains a pair of bounds α <: S and α <: T, and there
			 * exists a supertype of S of the form G<S1, ..., Sn> and a supertype of T
			 * of the form G<T1, ..., Tn> (for some generic class or interface, G),
			 * then for all i (1 ≤ i ≤ n), if Si and Ti are types (not wildcards), the
			 * constraint formula ‹Si = Ti› is implied.
			 */
			for (Type upperBound : upperBounds)
				incorporateSupertypeParameterizationEquality(type, upperBound);

			for (CaptureConversion captureConversion : boundSet
					.getCaptureConversions()) {
				Type capturedArgument = captureConversion.getCapturedArgument(a);
				TypeVariable<?> capturedParmeter = captureConversion
						.getCapturedParameter(a);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSubtype(captureConversion,
							(WildcardType) capturedArgument, capturedParmeter, type);
			}
		}
	}

	void addUpperBound(InferenceVariable type) {
		logBound(a, type, "<:");

		if (upperBounds.add(type)) {
			/*
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type equality : equalities)
				incorporateSubtypeSubstitution(equality, type);

			/*
			 * S <: α and α <: T imply ‹S <: T›
			 */
			for (Type lowerBound : lowerBounds)
				incorporateTransitiveSubtype(lowerBound, type);
		}
	}

	void addLowerBound(Type type) {
		logBound(type, a, "<:");

		if (lowerBounds.add(type)) {
			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBounds otherBounds = boundSet.getBoundsOn(other);

				for (Type equality : new HashSet<>(otherBounds.equalities))
					if (equality != type)
						boundSet.getBoundsOn(otherBounds.a)
								.incorporateProperSupertypeSubstitution(equality, type, a);
			}

			/*
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type equality : equalities)
				incorporateSupertypeSubstitution(equality, type);

			/*
			 * S <: α and α <: T imply ‹S <: T›
			 */
			for (Type upperBound : upperBounds)
				incorporateTransitiveSubtype(type, upperBound);

			for (CaptureConversion captureConversion : boundSet
					.getCaptureConversions()) {
				Type capturedArgument = captureConversion.getCapturedArgument(a);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSupertype((WildcardType) capturedArgument, type);
			}
		}
	}

	void addLowerBound(InferenceVariable type) {
		logBound(type, a, "<:");

		if (lowerBounds.add(type)) {
			/*
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type equality : equalities)
				incorporateSupertypeSubstitution(equality, type);

			/*
			 * S <: α and α <: T imply ‹S <: T›
			 */
			for (Type upperBound : upperBounds)
				incorporateTransitiveSubtype(type, upperBound);
		}
	}

	private void logBound(Type from, Type to, String boundString) {
		// System.out.println(System.identityHashCode(boundSet) + "  " + from + " "
		// + boundString + " " + to);
	}

	/*
	 * (In this section, S and T are inference variables or types, and U is a
	 * proper type. For conciseness, a bound of the form α = T may also match a
	 * bound of the form T = α.)
	 * 
	 * When a bound set contains a pair of bounds that match one of the following
	 * rules, a new constraint formula is implied:
	 */

	/*
	 * α = S and α = T imply ‹S = T›
	 */
	void incorporateTransitiveEquality(Type S, Type T) {
		ConstraintFormula.reduce(Kind.EQUALITY, S, T, boundSet);
	}

	/*
	 * α = S and α <: T imply ‹S <: T›
	 */
	void incorporateSubtypeSubstitution(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
	}

	/*
	 * α = S and T <: α imply ‹T <: S›
	 */
	void incorporateSupertypeSubstitution(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, T, S, boundSet);
	}

	/*
	 * S <: α and α <: T imply ‹S <: T›
	 */
	void incorporateTransitiveSubtype(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
	}

	/*
	 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
	 */
	void incorporateProperEqualitySubstitution(InferenceVariable a, Type U,
			InferenceVariable S, Type T) {
		incorporateProperEqualitySubstitutionImpl(a, U, S, T);
		incorporateProperEqualitySubstitutionImpl(S, T, a, U);
	}

	void incorporateProperEqualitySubstitutionImpl(InferenceVariable a, Type U,
			InferenceVariable S, Type T) {
		if (boundSet.isProperType(U)
				&& !Types.getAllMentionedBy(T, a::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(a, U);

			T = resolver.resolve(T);

			ConstraintFormula.reduce(Kind.EQUALITY, S, T, boundSet);
		}
	}

	/*
	 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
	 */
	void incorporateProperSubtypeSubstitution(Type U, InferenceVariable S, Type T) {
		if (boundSet.isProperType(U)
				&& !Types.getAllMentionedBy(T, a::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(a, U);

			T = resolver.resolve(T);

			ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
		}
	}

	void incorporateProperSupertypeSubstitution(Type U, Type S,
			InferenceVariable T) {
		if (boundSet.isProperType(U)
				&& !Types.getAllMentionedBy(S, a::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(a, U);

			S = resolver.resolve(S);

			ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
		}
	}

	/*
	 * When a bound set contains a pair of bounds α <: S and α <: T, and there
	 * exists a supertype of S of the form G<S1, ..., Sn> and a supertype of T of
	 * the form G<T1, ..., Tn> (for some generic class or interface, G), then for
	 * all i (1 ≤ i ≤ n), if Si and Ti are types (not wildcards), the constraint
	 * formula ‹Si = Ti› is implied.
	 */
	void incorporateSupertypeParameterizationEquality(Type S, Type T) {
		if (S.equals(T) || S.equals(Object.class) || T.equals(Object.class))
			return;

		new TypeVisitor() {
			@Override
			protected void visitClass(Class<?> type) {
				if (type.isAssignableFrom(Types.getRawType(T))) {
					Type supertypeS = ParameterizedTypes.resolveSupertypeParameters(S,
							type);
					Type supertypeT = ParameterizedTypes.resolveSupertypeParameters(T,
							type);

					for (TypeVariable<?> parameter : ParameterizedTypes
							.getAllTypeParameters(type)) {
						Type argumentS = ParameterizedTypes.getAllTypeArguments(
								(ParameterizedType) supertypeS).get(parameter);
						Type argumentT = ParameterizedTypes.getAllTypeArguments(
								(ParameterizedType) supertypeT).get(parameter);

						if (!(argumentS instanceof WildcardType)
								&& !(argumentT instanceof WildcardType))
							ConstraintFormula.reduce(Kind.EQUALITY, argumentS, argumentT,
									boundSet);
					}
				} else {
					visit(type.getInterfaces());
					visit(type.getSuperclass());
				}
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				visit(type.getRawType());
			}
		}.visit(S);
	}

	/*
	 * When a bound set contains a bound of the form G<α1, ..., αn> =
	 * capture(G<A1, ..., An>), new bounds are implied and new constraint formulas
	 * may be implied, as follows.
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

	void incorporateCapturedEquality(WildcardType A, Type R) {
		/*
		 * αi = R implies the bound false
		 */
		if (a.equals(R))
			boundSet.incorporate().falsehood();
	}

	void incorporateCapturedSubtype(CaptureConversion c, WildcardType A,
			TypeVariable<?> P, Type R) {
		TypeSubstitution θ = new TypeSubstitution();
		for (InferenceVariable variable : c.getInferenceVariables())
			θ = θ.where(c.getCapturedParameter(variable), variable);

		Type[] B = P.getBounds();

		if (A.getUpperBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? extends T:
			 */

			if (B.length == 0 || (B.length == 1 && B[0].equals(Object.class))) {
				/*
				 * If Bi is Object, then αi <: R implies the constraint formula ‹T <: R›
				 */
				ConstraintFormula.reduce(Kind.SUBTYPE,
						IntersectionType.from(A.getUpperBounds()), R, boundSet);
			}

			Type[] T = A.getUpperBounds();
			if (T.length == 0 || (T.length == 1 && T[0].equals(Object.class))) {
				/*
				 * If T is Object, then αi <: R implies the constraint formula ‹Bi θ <:
				 * R›
				 */
				ConstraintFormula.reduce(Kind.SUBTYPE,
						θ.resolve(IntersectionType.from(B)), R, boundSet);
			}
		} else if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE,
					θ.resolve(IntersectionType.from(B)), R, boundSet);
		} else {
			/*
			 * If Ai is a wildcard of the form ?:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE,
					θ.resolve(IntersectionType.from(B)), R, boundSet);
		}
	}

	void incorporateCapturedSupertype(WildcardType A, Type R) {
		if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * R <: αi implies the constraint formula ‹R <: T›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE, R,
					IntersectionType.uncheckedFrom(A.getLowerBounds()), boundSet);
		} else if (A.getUpperBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? extends T:
			 * 
			 * R <: αi implies the bound false
			 */
			boundSet.incorporate().falsehood();
		} else {
			/*
			 * If Ai is a wildcard of the form ?:
			 * 
			 * R <: αi implies the bound false
			 */
			boundSet.incorporate().falsehood();
		}
	}
}
