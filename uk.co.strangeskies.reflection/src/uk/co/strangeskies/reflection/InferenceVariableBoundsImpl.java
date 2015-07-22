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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

class InferenceVariableBoundsImpl implements InferenceVariableBounds {
	private final BoundSet boundSet;

	private final InferenceVariable inferenceVariable;
	private Set<Type> equalities;
	private Set<Type> upperBounds;
	private Set<Type> lowerBounds;

	private CaptureConversion capture;

	private Set<InferenceVariable> allDependencies;
	private Set<InferenceVariable> externalDependencies;
	private Set<InferenceVariable> relations;

	public InferenceVariableBoundsImpl(BoundSet boundSet,
			InferenceVariable inferenceVariable) {
		this.boundSet = boundSet;
		this.inferenceVariable = inferenceVariable;

		upperBounds = new HashSet<>();
		lowerBounds = new HashSet<>();
		equalities = new HashSet<>();
		equalities.add(inferenceVariable);

		relations = new HashSet<>();
		relations.add(inferenceVariable);

		allDependencies = new HashSet<>();
		allDependencies.add(inferenceVariable);
		externalDependencies = new HashSet<>();
	}

	public InferenceVariableBoundsImpl copyInto(BoundSet boundSet) {
		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(
				boundSet, inferenceVariable);

		copy.upperBounds.addAll(upperBounds);
		copy.lowerBounds.addAll(lowerBounds);
		copy.equalities.addAll(equalities);

		copy.capture = capture;

		copy.relations.addAll(relations);

		if (isInstantiated()) {
			copy.allDependencies = null;
			copy.externalDependencies = null;
		} else {
			copy.allDependencies.clear();
			copy.allDependencies.addAll(allDependencies);
			copy.externalDependencies.addAll(externalDependencies);
		}

		return copy;
	}

	public void filter(Predicate<InferenceVariable> ignoring) {
		Iterator<Type> boundIterator = upperBounds.iterator();
		while (boundIterator.hasNext())
			if (boundSet.getInferenceVariablesMentionedBy(boundIterator.next())
					.stream().anyMatch(ignoring::test))
				boundIterator.remove();

		boundIterator = lowerBounds.iterator();
		while (boundIterator.hasNext())
			if (boundSet.getInferenceVariablesMentionedBy(boundIterator.next())
					.stream().anyMatch(ignoring::test))
				boundIterator.remove();

		boundIterator = equalities.iterator();
		while (boundIterator.hasNext())
			if (boundSet.getInferenceVariablesMentionedBy(boundIterator.next())
					.stream().anyMatch(ignoring::test))
				boundIterator.remove();

		if (capture != null
				&& !capture.getAllMentionedInferenceVariables(boundSet).stream()
						.anyMatch(ignoring::test))
			capture = null;

		Iterator<InferenceVariable> variableIterator = relations.iterator();
		while (variableIterator.hasNext())
			if (ignoring.test(variableIterator.next()))
				variableIterator.remove();

		if (!isInstantiated()) {
			variableIterator = allDependencies.iterator();
			while (variableIterator.hasNext())
				if (ignoring.test(variableIterator.next()))
					variableIterator.remove();

			variableIterator = externalDependencies.iterator();
			while (variableIterator.hasNext())
				if (ignoring.test(variableIterator.next()))
					variableIterator.remove();
		}
	}

	public InferenceVariableBoundsImpl withInferenceVariableSubstitution(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions) {
		InferenceVariable aSubstitution = inferenceVariableSubstitutions
				.get(inferenceVariable);
		if (aSubstitution == null)
			aSubstitution = inferenceVariable;

		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(
				boundSet, aSubstitution);

		copy.allDependencies.clear();
		copy.addBoundsWithTypeSubstitution(this, new TypeSubstitution(
				inferenceVariableSubstitutions));

		return copy;
	}

	private void addBoundsWithTypeSubstitution(
			InferenceVariableBoundsImpl inferenceVariableBounds,
			TypeSubstitution where) {
		inferenceVariableBounds.equalities.stream().map(where::resolve)
				.forEach(equalities::add);
		inferenceVariableBounds.upperBounds.stream().map(where::resolve)
				.forEach(upperBounds::add);
		inferenceVariableBounds.lowerBounds.stream().map(where::resolve)
				.forEach(lowerBounds::add);

		capture = inferenceVariableBounds.capture;

		inferenceVariableBounds.relations.stream().map(where::resolve)
				.map(InferenceVariable.class::cast).forEach(relations::add);

		inferenceVariableBounds.allDependencies.stream().map(where::resolve)
				.map(InferenceVariable.class::cast).forEach(allDependencies::add);
		inferenceVariableBounds.externalDependencies.stream().map(where::resolve)
				.map(InferenceVariable.class::cast).forEach(externalDependencies::add);
	}

	@Override
	public CaptureConversion getCaptureConversion() {
		return capture;
	}

	public void addCaptureConversion(CaptureConversion captureConversion) {
		if (capture != null)
			throw new TypeException("Inference variable '" + inferenceVariable
					+ "' Cannot be captured by two capture conversions '" + capture
					+ "' and '" + captureConversion + "' simultaniously.");
		else
			capture = captureConversion;

		if (!isInstantiated()) {
			allDependencies.clear();

			allDependencies.add(inferenceVariable);
			allDependencies.addAll(externalDependencies);

			/*
			 * Given a bound of one of the following forms, where T is either an
			 * inference variable β or a type that mentions β:
			 * 
			 * α = T
			 * 
			 * α <: T
			 * 
			 * T = α
			 * 
			 * T <: α
			 */
			Stream<InferenceVariableBoundsImpl> mentions = getRelated().stream()
					.map(mention -> boundSet.getBoundsOn(mention))
					.filter(mention -> !mention.isInstantiated() && !(mention == this));
			if (capture != null) {
				/*
				 * If α appears on the left-hand side of another bound of the form
				 * G<..., α, ...> = capture(G<...>), then β depends on the resolution of
				 * α.
				 */
				mentions.forEach(dependencies -> dependencies
						.addExternalDependencies(allDependencies));
			} else {
				/*
				 * Otherwise, α depends on the resolution of β.
				 */
				mentions.map(mention -> mention.allDependencies).forEach(
						dependencies -> allDependencies.addAll(dependencies));
			}

			/*
			 * An inference variable α appearing on the left-hand side of a bound of
			 * the form G<..., α, ...> = capture(G<...>) depends on the resolution of
			 * every other inference variable mentioned in this bound (on both sides
			 * of the = sign).
			 */
			for (InferenceVariable inferenceVariable : capture
					.getAllMentionedInferenceVariables(boundSet)) {
				allDependencies
						.addAll(boundSet.getBoundsOn(inferenceVariable).allDependencies);
			}
		}
	}

	public void removeCaptureConversion() {
		/*
		 * No need to modify dependencies here, as capture conversion will only be
		 * removed alongside an instantiation, so dependencies will be cleared
		 * through that mechanism.
		 */
		capture = null;
	}

	private void addExternalDependencies(Set<InferenceVariable> allMentioned) {
		if (!isInstantiated()) {
			for (InferenceVariable mentioned : allMentioned) {
				if (!boundSet.getBoundsOn(mentioned).isInstantiated()) {
					allDependencies.add(mentioned);
					externalDependencies.add(mentioned);
				}
			}
		}
	}

	private void removeDependencies(Set<InferenceVariable> allDependencies) {
		if (!isInstantiated()) {
			this.allDependencies.removeAll(allDependencies);
			this.externalDependencies.removeAll(allDependencies);

			if (this.allDependencies.isEmpty()) {
				this.allDependencies = null;
				this.externalDependencies = null;
			}
		}
	}

	private void addMentions(Collection<InferenceVariable> mentions) {
		this.relations.addAll(mentions);
		for (InferenceVariable relation : relations)
			boundSet.getBoundsOn(relation).relations.addAll(mentions);

		if (!isInstantiated()) {
			/*
			 * Given a bound of one of the following forms, where T is either an
			 * inference variable β or a type that mentions β:
			 * 
			 * α = T
			 * 
			 * α <: T
			 * 
			 * T = α
			 * 
			 * T <: α
			 */
			if (capture != null) {
				/*
				 * If α appears on the left-hand side of another bound of the form
				 * G<..., α, ...> = capture(G<...>), then β depends on the resolution of
				 * α.
				 */
				for (InferenceVariable mention : mentions) {
					if (!boundSet.getBoundsOn(mention).isInstantiated())
						boundSet.getBoundsOn(mention).addExternalDependencies(
								allDependencies);
				}
			} else {
				/*
				 * Otherwise, α depends on the resolution of β.
				 */
				for (InferenceVariable mention : mentions) {
					if (!boundSet.getBoundsOn(mention).isInstantiated())
						allDependencies
								.addAll(boundSet.getBoundsOn(mention).allDependencies);
				}
			}
		}
	}

	@Override
	public InferenceVariable getInferenceVariable() {
		return inferenceVariable;
	}

	@Override
	public Set<Type> getEqualities() {
		return Collections.unmodifiableSet(equalities);
	}

	@Override
	public Set<Type> getUpperBounds() {
		return Collections.unmodifiableSet(upperBounds);
	}

	@Override
	public Set<Type> getLowerBounds() {
		return Collections.unmodifiableSet(lowerBounds);
	}

	@Override
	public Set<Type> getProperUpperBounds() {
		Set<Type> upperBounds = getUpperBounds().stream()
				.filter(boundSet::isProperType).collect(Collectors.toSet());
		return upperBounds.isEmpty() ? new HashSet<>(Arrays.asList(Object.class))
				: upperBounds;
	}

	@Override
	public Set<Type> getProperLowerBounds() {
		return getLowerBounds().stream().filter(boundSet::isProperType)
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<Type> getInstantiation() {
		return getEqualities().stream().filter(boundSet::isProperType).findAny();
	}

	@Override
	public boolean isInstantiated() {
		return allDependencies == null;
	}

	@Override
	public Set<InferenceVariable> getRemainingDependencies() {
		if (!isInstantiated())
			return Collections.unmodifiableSet(allDependencies);
		else
			return Collections.emptySet();
	}

	@Override
	public Set<InferenceVariable> getRelated() {
		return Collections.unmodifiableSet(relations);
	}

	public void addEquality(Type type) {
		if (boundSet.isInferenceVariable(type))
			addEquality((InferenceVariable) type);
		else if (this.equalities.add(type)) {
			logBound(inferenceVariable, type, "=");

			Set<Type> equalities = new HashSet<>(this.equalities);
			Set<Type> upperBounds = new HashSet<>(this.upperBounds);
			Set<Type> lowerBounds = new HashSet<>(this.lowerBounds);

			Set<InferenceVariable> mentions = boundSet
					.getInferenceVariablesMentionedBy(type);
			if (mentions.isEmpty()) {
				/*
				 * An instantiation has been found.
				 */

				if (isInstantiated()) {
					ConstraintFormula.reduce(Kind.EQUALITY, type, getInstantiation()
							.get(), boundSet);
				} else {
					if (capture != null)
						boundSet.incorporate().falsehood();

					Set<InferenceVariable> oldDependencies = new HashSet<>(
							this.allDependencies);
					allDependencies = null;
					externalDependencies = null;
					for (InferenceVariable relation : relations) {
						boundSet.getBoundsOn(relation).removeDependencies(oldDependencies);
					}
				}
			} else {
				addMentions(mentions);
			}

			/*
			 * α = S and α = T imply ‹S = T›
			 */
			for (Type equality : equalities)
				incorporateTransitiveEquality(type, equality);

			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOn(other);

				/*
				 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
				 */
				for (Type equality : new HashSet<>(otherBounds.getEqualities()))
					if (equality != type)
						incorporateProperEqualitySubstitution(inferenceVariable, type,
								otherBounds.inferenceVariable, equality);

				/*
				 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
				 */
				for (Type supertype : new HashSet<>(otherBounds.getUpperBounds()))
					if (supertype != type)
						incorporateProperSubtypeSubstitution(type,
								otherBounds.inferenceVariable, supertype);
				for (Type subtype : new HashSet<>(otherBounds.getLowerBounds()))
					if (subtype != type)
						incorporateProperSupertypeSubstitution(type, subtype,
								otherBounds.inferenceVariable);
			}

			/*
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type supertype : upperBounds)
				incorporateSubtypeSubstitution(type, supertype);

			/*
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type subtype : lowerBounds)
				incorporateSupertypeSubstitution(type, subtype);

			for (CaptureConversion captureConversion : boundSet
					.getCaptureConversions()) {
				Type capturedArgument = captureConversion
						.getCapturedArgument(inferenceVariable);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedEquality((WildcardType) capturedArgument, type);
			}
		}
	}

	private void addEquality(InferenceVariable type) {
		if (this.equalities.add(type)) {
			logBound(inferenceVariable, type, "=");

			addMentions(Arrays.asList(type));

			InferenceVariableBoundsImpl thoseBounds = boundSet.getBoundsOn(type);

			/*-
			 * α = S and α = T imply ‹S = T›
			 */
			for (Type equality : new HashSet<>(equalities))
				incorporateTransitiveEquality(type, equality);

			thoseBounds.equalities = equalities;

			/*-
			 * α = S and α <: T imply ‹S <: T›
			 */
			for (Type supertype : new HashSet<>(upperBounds))
				incorporateSubtypeSubstitution(type, supertype);

			thoseBounds.upperBounds = upperBounds;

			/*-
			 * α = S and T <: α imply ‹T <: S›
			 */
			for (Type subtype : new HashSet<>(lowerBounds))
				incorporateSupertypeSubstitution(type, subtype);

			thoseBounds.lowerBounds = lowerBounds;

			thoseBounds.allDependencies = allDependencies;
			thoseBounds.externalDependencies = externalDependencies;
			thoseBounds.relations = relations;
		}
	}

	public void addUpperBound(Type type) {
		if (boundSet.isInferenceVariable(type))
			addUpperBound((InferenceVariable) type);
		else if (this.upperBounds.add(type)) {
			logBound(inferenceVariable, type, "<:");

			addMentions(boundSet.getInferenceVariablesMentionedBy(type));

			Set<Type> equalities = new HashSet<>(this.equalities);
			Set<Type> upperBounds = new HashSet<>(this.upperBounds);
			Set<Type> lowerBounds = new HashSet<>(this.lowerBounds);

			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOn(other);

				for (Type equality : new HashSet<>(otherBounds.equalities))
					if (equality != type)
						boundSet.getBoundsOn(otherBounds.inferenceVariable)
								.incorporateProperSubtypeSubstitution(equality,
										inferenceVariable, type);
			}

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
				Type capturedArgument = captureConversion
						.getCapturedArgument(inferenceVariable);
				TypeVariable<?> capturedParmeter = captureConversion
						.getCapturedParameter(inferenceVariable);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSubtype(captureConversion,
							(WildcardType) capturedArgument, capturedParmeter, type);
			}
		}
	}

	private void addUpperBound(InferenceVariable type) {
		if (upperBounds.add(type)) {
			logBound(inferenceVariable, type, "<:");

			addMentions(Arrays.asList(type));
			boundSet.getBoundsOn(type).addMentions(Arrays.asList(inferenceVariable));

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

	public void addLowerBound(Type type) {
		if (boundSet.isInferenceVariable(type))
			addLowerBound((InferenceVariable) type);
		else if (lowerBounds.add(type)) {
			logBound(type, inferenceVariable, "<:");

			addMentions(boundSet.getInferenceVariablesMentionedBy(type));

			Set<Type> equalities = new HashSet<>(this.equalities);
			Set<Type> upperBounds = new HashSet<>(this.upperBounds);

			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOn(other);

				for (Type equality : new HashSet<>(otherBounds.equalities))
					if (equality != type)
						boundSet.getBoundsOn(otherBounds.inferenceVariable)
								.incorporateProperSupertypeSubstitution(equality, type,
										inferenceVariable);
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
				Type capturedArgument = captureConversion
						.getCapturedArgument(inferenceVariable);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSupertype((WildcardType) capturedArgument, type);
			}
		}
	}

	private void addLowerBound(InferenceVariable type) {
		if (lowerBounds.add(type)) {
			logBound(type, inferenceVariable, "<:");

			addMentions(Arrays.asList(type));
			boundSet.getBoundsOn(type).addMentions(Arrays.asList(inferenceVariable));

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
	public void incorporateTransitiveEquality(Type S, Type T) {
		ConstraintFormula.reduce(Kind.EQUALITY, S, T, boundSet);
	}

	/*
	 * α = S and α <: T imply ‹S <: T›
	 */
	public void incorporateSubtypeSubstitution(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
	}

	/*
	 * α = S and T <: α imply ‹T <: S›
	 */
	public void incorporateSupertypeSubstitution(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, T, S, boundSet);
	}

	/*
	 * S <: α and α <: T imply ‹S <: T›
	 */
	public void incorporateTransitiveSubtype(Type S, Type T) {
		ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
	}

	/*
	 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
	 */
	public void incorporateProperEqualitySubstitution(InferenceVariable a,
			Type U, InferenceVariable S, Type T) {
		incorporateProperEqualitySubstitutionImpl(a, U, S, T);
		incorporateProperEqualitySubstitutionImpl(S, T, a, U);
	}

	public void incorporateProperEqualitySubstitutionImpl(InferenceVariable a,
			Type U, InferenceVariable S, Type T) {
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
	public void incorporateProperSubtypeSubstitution(Type U, InferenceVariable S,
			Type T) {
		if (boundSet.isProperType(U)
				&& !Types.getAllMentionedBy(T, inferenceVariable::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(
					inferenceVariable, U);

			T = resolver.resolve(T);

			ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
		}
	}

	public void incorporateProperSupertypeSubstitution(Type U, Type S,
			InferenceVariable T) {
		if (boundSet.isProperType(U)
				&& !Types.getAllMentionedBy(S, inferenceVariable::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(
					inferenceVariable, U);

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
	public void incorporateSupertypeParameterizationEquality(Type S, Type T) {
		if (S.equals(T) || S.equals(Object.class) || T.equals(Object.class))
			return;

		if (S instanceof IntersectionType)
			for (Type itemS : ((IntersectionType) S).getTypes())
				incorporateSupertypeParameterizationEquality(itemS, T);
		else if (T instanceof IntersectionType)
			for (Type itemT : ((IntersectionType) T).getTypes())
				incorporateSupertypeParameterizationEquality(S, itemT);
		else
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

	public void incorporateCapturedEquality(WildcardType A, Type R) {
		/*
		 * αi = R implies the bound false
		 */
		if (inferenceVariable.equals(R))
			boundSet.incorporate().falsehood();
	}

	public void incorporateCapturedSubtype(CaptureConversion c, WildcardType A,
			TypeVariable<?> P, Type R) {
		TypeSubstitution θ = new TypeSubstitution();
		for (InferenceVariable variable : c.getInferenceVariables())
			θ = θ.where(c.getCapturedParameter(variable), variable);

		Type[] B = P.getBounds();

		if (A.getUpperBounds().length > 0) {
			Type[] T = A.getUpperBounds();

			/*
			 * If Ai is a wildcard of the form ? extends T:
			 */

			if (T.length == 0 || (T.length == 1 && T[0].equals(Object.class))) {
				/*
				 * If T is Object, then αi <: R implies the constraint formula ‹Bi θ <:
				 * R›
				 */
				ConstraintFormula.reduce(Kind.SUBTYPE,
						θ.resolve(IntersectionType.from(B)), R, boundSet);
			}

			if (B.length == 0 || (B.length == 1 && B[0].equals(Object.class))) {
				/*
				 * If Bi is Object, then αi <: R implies the constraint formula ‹T <: R›
				 */
				ConstraintFormula.reduce(Kind.SUBTYPE, IntersectionType.from(T), R,
						boundSet);
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

	public void incorporateCapturedSupertype(WildcardType A, Type R) {
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
			 * Else:
			 * 
			 * If Ai is a wildcard of the form ?:
			 * 
			 * R <: αi implies the bound false
			 */
			boundSet.incorporate().falsehood();
		}
	}
}