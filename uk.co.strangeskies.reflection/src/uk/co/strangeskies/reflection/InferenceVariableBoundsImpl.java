/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import uk.co.strangeskies.utilities.IdentityProperty;

class InferenceVariableBoundsImpl implements InferenceVariableBounds {
	private final BoundSet boundSet;

	private final InferenceVariable inferenceVariable;
	private Type instantiation;

	private Set<Type> equalities;
	private Set<Type> upperBounds;
	private Set<Type> lowerBounds;

	private CaptureConversion capture;

	private Set<InferenceVariable> relations;

	private Set<InferenceVariable> remainingDependencies;

	public InferenceVariableBoundsImpl(BoundSet boundSet, InferenceVariable inferenceVariable) {
		this.boundSet = boundSet;
		this.inferenceVariable = inferenceVariable;

		upperBounds = new HashSet<>();
		lowerBounds = new HashSet<>();
		equalities = new HashSet<>();
		equalities.add(inferenceVariable);

		relations = new HashSet<>();
		relations.add(inferenceVariable);
	}

	BoundSet getBoundSet() {
		return boundSet;
	}

	public InferenceVariableBoundsImpl copyInto(BoundSet boundSet) {
		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(boundSet, inferenceVariable);

		copy.capture = capture;

		if (boundSet.containsInferenceVariable(inferenceVariable))
			throw new ReflectionException(p -> p.cannotCopyInferenceVariable(inferenceVariable, boundSet));

		for (Type equality : equalities) {
			if (boundSet.containsInferenceVariable(equality)) {
				InferenceVariableBoundsImpl bounds = boundSet.getBoundsOnImpl((InferenceVariable) equality);

				copy.equalities = bounds.equalities;
				copy.upperBounds = bounds.upperBounds;
				copy.lowerBounds = bounds.lowerBounds;

				copy.relations = bounds.relations;

				copy.instantiation = bounds.instantiation;

				return copy;
			}
		}

		copy.upperBounds.addAll(upperBounds);
		copy.lowerBounds.addAll(lowerBounds);
		copy.equalities.addAll(equalities);

		copy.relations.addAll(relations);

		copy.instantiation = instantiation;

		return copy;
	}

	public InferenceVariableBoundsImpl copyIntoFiltered(BoundSet boundSet, Predicate<InferenceVariable> ignoring) {
		InferenceVariableBoundsImpl copy = copyInto(boundSet);

		copy.filter(this.boundSet, ignoring);

		return copy;
	}

	public void filter(BoundSet boundSet, Predicate<InferenceVariable> ignoring) {
		Iterator<Type> boundIterator = upperBounds.iterator();
		while (boundIterator.hasNext())
			if (InferenceVariable.getMentionedBy(boundIterator.next()).stream().anyMatch(ignoring::test))
				boundIterator.remove();

		boundIterator = lowerBounds.iterator();
		while (boundIterator.hasNext())
			if (InferenceVariable.getMentionedBy(boundIterator.next()).stream().anyMatch(ignoring::test))
				boundIterator.remove();

		boundIterator = equalities.iterator();
		while (boundIterator.hasNext())
			if (InferenceVariable.getMentionedBy(boundIterator.next()).stream().anyMatch(ignoring::test))
				boundIterator.remove();

		if (capture != null && capture.getInferenceVariablesMentioned().stream().anyMatch(ignoring::test))
			throw new ReflectionException(p -> p.cannotFilterCapture(capture));

		Iterator<InferenceVariable> variableIterator = relations.iterator();
		while (variableIterator.hasNext())
			if (ignoring.test(variableIterator.next()))
				variableIterator.remove();
	}

	public InferenceVariableBoundsImpl withInferenceVariableSubstitution(
			Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions) {
		InferenceVariable aSubstitution = inferenceVariableSubstitutions.get(inferenceVariable);
		if (aSubstitution == null)
			aSubstitution = inferenceVariable;

		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(boundSet, aSubstitution);

		copy.addBoundsWithTypeSubstitution(this, new TypeSubstitution(inferenceVariableSubstitutions));

		return copy;
	}

	private void addBoundsWithTypeSubstitution(InferenceVariableBoundsImpl inferenceVariableBounds,
			TypeSubstitution where) {
		inferenceVariableBounds.equalities.stream().map(where::resolve).forEach(equality -> {
			equalities.add(equality);
			if (InferenceVariable.getMentionedBy(equality).isEmpty()) {
				instantiation = equality;
			}
		});
		inferenceVariableBounds.upperBounds.stream().map(where::resolve).forEach(upperBounds::add);
		inferenceVariableBounds.lowerBounds.stream().map(where::resolve).forEach(lowerBounds::add);

		capture = inferenceVariableBounds.capture;

		inferenceVariableBounds.relations.stream().map(where::resolve).map(InferenceVariable.class::cast)
				.forEach(relations::add);
	}

	@Override
	public CaptureConversion getCaptureConversion() {
		return capture;
	}

	public void addCaptureConversion(CaptureConversion captureConversion) {
		if (capture != null)
			throw new ReflectionException(p -> p.cannotCaptureMultipleTimes(inferenceVariable, capture, captureConversion));
		else
			capture = captureConversion;

		invalidateDependencies();

		Set<InferenceVariable> mentions = captureConversion.getInferenceVariablesMentioned();

		for (InferenceVariable mention : mentions)
			this.relations.addAll(boundSet.getBoundsOnImpl(mention).relations);

		for (InferenceVariable mention : mentions)
			boundSet.getBoundsOnImpl(mention).relations.addAll(relations);
	}

	public void removeCaptureConversion() {
		if (!getInstantiation().isPresent())
			throw new IllegalStateException(
					"Capture conversion '" + capture + "' should not be removed with no instantiation for '" + inferenceVariable
							+ "' in bound set '" + boundSet + "'");

		if (capture == null)
			throw new IllegalStateException("Attempt to remove missing capture from '" + capture + "'");

		/*
		 * No need to modify dependencies here, as capture conversion will only be
		 * removed alongside an instantiation, so dependencies will be cleared
		 * through that mechanism.
		 */
		capture = null;
	}

	private void addMentions(Collection<InferenceVariable> mentions) {
		invalidateDependencies();

		for (InferenceVariable mention : mentions)
			this.relations.addAll(boundSet.getBoundsOnImpl(mention).relations);

		for (InferenceVariable relation : relations)
			boundSet.getBoundsOnImpl(relation).relations.addAll(relations);
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
		Set<Type> upperBounds = getUpperBounds().stream().filter(InferenceVariable::isProperType)
				.collect(Collectors.toSet());
		return upperBounds.isEmpty() ? new HashSet<>(Arrays.asList(Object.class)) : upperBounds;
	}

	@Override
	public Set<Type> getProperLowerBounds() {
		return getLowerBounds().stream().filter(InferenceVariable::isProperType).collect(Collectors.toSet());
	}

	@Override
	public Optional<Type> getInstantiation() {
		return Optional.ofNullable(instantiation);
	}

	private void invalidateDependencies() {
		boundSet.getInferenceVariables().stream().map(boundSet::getBoundsOnImpl)
				.filter(b -> b.remainingDependencies != null && b.remainingDependencies.contains(inferenceVariable))
				.forEach(b -> b.remainingDependencies = null);
		remainingDependencies = null;
	}

	@Override
	public Set<InferenceVariable> getRemainingDependencies() {
		Set<InferenceVariableBoundsImpl> recalculated = new HashSet<>();

		if (remainingDependencies == null) {
			recalculateRemainingDependencies();
			recalculated.add(this);
		}

		/*
		 * Inference variables which are part of capture conversions may create
		 * dependencies on this, so we should recalculate any which are dirty.
		 */
		Stream
				.concat(remainingDependencies.stream(),
						boundSet.getCaptureConversions().stream().flatMap(c -> c.getInferenceVariables().stream()))
				.distinct().filter(d -> !d.equals(inferenceVariable)).map(boundSet::getBoundsOnImpl)
				.filter(b -> b.remainingDependencies == null && !b.getInstantiation().isPresent()).forEach(d -> {
					d.recalculateRemainingDependencies();
					recalculated.add(d);
				});

		/*
		 * An inference variable α depends on the resolution of an inference
		 * variable β if there exists an inference variable γ such that α depends on
		 * the resolution of γ and γ depends on the resolution of β.
		 */
		IdentityProperty<Boolean> added = new IdentityProperty<>();
		do {
			added.set(false);
			for (InferenceVariableBoundsImpl bounds : recalculated) {
				for (InferenceVariable dependency : new HashSet<>(bounds.remainingDependencies)) {
					if (bounds.remainingDependencies.addAll(boundSet.getBoundsOnImpl(dependency).getRemainingDependencies())) {
						added.set(true);
					}
				}
			}
		} while (added.get());

		return remainingDependencies;
	}

	private void recalculateRemainingDependencies() {
		if (remainingDependencies == null) {
			if (getInstantiation().isPresent()) {
				remainingDependencies = Collections.emptySet();
			} else {
				remainingDependencies = new HashSet<>();
				/*
				 * An inference variable α depends on the resolution of itself.
				 */
				remainingDependencies.add(inferenceVariable);

				Stream<InferenceVariableBoundsImpl> mentions = Stream
						.concat(Stream.concat(upperBounds.stream(), lowerBounds.stream()), equalities.stream())
						.map(InferenceVariable::getMentionedBy).flatMap(Set::stream).map(boundSet::getBoundsOnImpl)
						.filter(mention -> !mention.getInstantiation().isPresent());

				if (capture != null) {
					/*
					 * An inference variable α appearing on the left-hand side of a bound
					 * of the form G<..., α, ...> = capture(G<...>) depends on the
					 * resolution of every other inference variable mentioned in this
					 * bound (on both sides of the = sign).
					 */

					remainingDependencies.addAll(capture.getInferenceVariablesMentioned());

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
					 * 
					 * If α appears on the left-hand side of another bound of the form
					 * G<..., α, ...> = capture(G<...>), then β depends on the resolution
					 * of α...
					 */
					mentions.forEach(b -> b.remainingDependencies.add(inferenceVariable));
				} else {
					/*
					 * ...Otherwise, α depends on the resolution of β.
					 */
					mentions.map(b -> b.inferenceVariable).forEach(remainingDependencies::add);
				}
			}
		}
	}

	@Override
	public Set<InferenceVariable> getRelated() {
		return Collections.unmodifiableSet(relations);
	}

	void addEquality(Type type) {
		if (this.equalities.add(type)) {
			logBound(inferenceVariable, type, "=");

			Set<InferenceVariable> mentions;

			if (type instanceof InferenceVariable) {
				mentions = Collections.emptySet();
				addInferenceVariableEquality((InferenceVariable) type);
			} else {
				mentions = InferenceVariable.getMentionedBy(type);
				addTypeEquality(type, mentions);
			}

			addMentions(mentions);
		}
	}

	private void addTypeEquality(Type type, Set<InferenceVariable> mentions) {
		Set<Type> equalities = new HashSet<>(this.equalities);
		Set<Type> upperBounds = new HashSet<>(this.upperBounds);
		Set<Type> lowerBounds = new HashSet<>(this.lowerBounds);

		if (mentions.isEmpty()) {
			/*
			 * An instantiation has been found.
			 */

			if (getInstantiation().isPresent()) {
				/*
				 * If we already have an instantiation, make sure the new one is equal
				 * to it...
				 */
				ConstraintFormula.reduce(Kind.EQUALITY, type, getInstantiation().get(), new BoundSet());
			} else {
				/*
				 * ...Otherwise, make sure there are no captures present, and remove all
				 * remaining dependencies on this inference variable, and all those
				 * equal to it.
				 */
				if (capture != null)
					boundSet.incorporate().falsehood("Cannot add instantiation with capture conversion present: " + this);

				instantiation = type;

				equalities.stream().filter(InferenceVariable.class::isInstance).map(InferenceVariable.class::cast)
						.map(boundSet::getBoundsOnImpl).forEach(equality -> {
							equality.instantiation = type;
						});
			}
		}

		/*
		 * α = S and α = T imply ‹S = T›
		 */
		for (Type equality : equalities)
			incorporateTransitiveEquality(type, equality);

		if (mentions.isEmpty()) {
			for (InferenceVariable other : boundSet.getInferenceVariables()) {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

				/*
				 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
				 */
				for (Type equality : new HashSet<>(otherBounds.getEqualities()))
					if (equality != type)
						incorporateProperEqualitySubstitution(type, otherBounds.inferenceVariable, equality);

				/*
				 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
				 */
				for (Type supertype : new HashSet<>(otherBounds.getUpperBounds()))
					if (supertype != type)
						incorporateProperSubtypeSubstitution(type, otherBounds.inferenceVariable, supertype);

				for (Type subtype : new HashSet<>(otherBounds.getLowerBounds()))
					if (subtype != type)
						incorporateProperSupertypeSubstitution(type, subtype, otherBounds.inferenceVariable);
			}
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

		for (CaptureConversion captureConversion : boundSet.getCaptureConversions()) {
			Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);

			if (capturedArgument instanceof WildcardType)
				incorporateCapturedEquality((WildcardType) capturedArgument, type);
		}

	}

	private void addInferenceVariableEquality(InferenceVariable type) {
		Set<Type> equalities = new HashSet<>(this.equalities);
		Set<Type> upperBounds = new HashSet<>(this.upperBounds);
		Set<Type> lowerBounds = new HashSet<>(this.lowerBounds);

		InferenceVariableBoundsImpl thoseBounds = boundSet.getBoundsOnImpl(type);

		/*-
		 * α = S and α <: T imply ‹S <: T›
		 */
		for (Type supertype : new HashSet<>(upperBounds))
			incorporateSubtypeSubstitution(type, supertype);

		for (Type supertype : new HashSet<>(thoseBounds.upperBounds))
			thoseBounds.incorporateSubtypeSubstitution(inferenceVariable, supertype);

		/*-
		 * α = S and T <: α imply ‹T <: S›
		 */
		for (Type subtype : new HashSet<>(lowerBounds))
			incorporateSupertypeSubstitution(type, subtype);

		for (Type subtype : new HashSet<>(thoseBounds.lowerBounds))
			thoseBounds.incorporateSupertypeSubstitution(inferenceVariable, subtype);

		/*-
		 * α = S and α = T imply ‹S = T›
		 */
		for (Type equality : new HashSet<>(equalities))
			incorporateTransitiveEquality(type, equality);

		for (Type equality : new HashSet<>(thoseBounds.equalities))
			thoseBounds.incorporateTransitiveEquality(inferenceVariable, equality);

		this.equalities.addAll(thoseBounds.equalities);
		this.upperBounds.addAll(thoseBounds.upperBounds);
		this.lowerBounds.addAll(thoseBounds.lowerBounds);
		this.relations.addAll(thoseBounds.relations);
		thoseBounds.equalities = this.equalities;
		thoseBounds.upperBounds = this.upperBounds;
		thoseBounds.lowerBounds = this.lowerBounds;
		thoseBounds.relations = this.relations;
	}

	void addUpperBound(Type type) {
		if (this.upperBounds.add(type)) {
			logBound(inferenceVariable, type, "<:");

			addMentions(InferenceVariable.getMentionedBy(type));

			Set<Type> equalities = new HashSet<>(this.equalities);
			Set<Type> upperBounds = new HashSet<>(this.upperBounds);
			Set<Type> lowerBounds = new HashSet<>(this.lowerBounds);

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

			if (!(type instanceof InferenceVariable)) {
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
				 * Capture conversions...
				 */
				for (CaptureConversion captureConversion : boundSet.getCaptureConversions()) {
					Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);
					TypeVariable<?> capturedParmeter = captureConversion.getCapturedParameter(inferenceVariable);

					if (capturedArgument instanceof WildcardType)
						incorporateCapturedSubtype(captureConversion, (WildcardType) capturedArgument, capturedParmeter, type);
				}

				/*
				 * When a bound set contains a pair of bounds α <: S and α <: T, and
				 * there exists a supertype of S of the form G<S1, ..., Sn> and a
				 * supertype of T of the form G<T1, ..., Tn> (for some generic class or
				 * interface, G), then for all i (1 ≤ i ≤ n), if Si and Ti are types
				 * (not wildcards), the constraint formula ‹Si = Ti› is implied.
				 */
				for (Type upperBound : upperBounds)
					incorporateSupertypeParameterizationEquality(type, upperBound);

				/*
				 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
				 */
				for (InferenceVariable other : boundSet.getInferenceVariables()) {
					InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

					for (Type equality : new HashSet<>(otherBounds.equalities))
						if (equality != type && InferenceVariable.isProperType(equality))
							boundSet.getBoundsOnImpl(otherBounds.inferenceVariable).incorporateProperSubtypeSubstitution(equality,
									inferenceVariable, type);
				}
			}
		}
	}

	void addLowerBound(Type type) {
		if (lowerBounds.add(type)) {
			logBound(type, inferenceVariable, "<:");

			addMentions(InferenceVariable.getMentionedBy(type));

			Set<Type> equalities = new HashSet<>(this.equalities);
			Set<Type> upperBounds = new HashSet<>(this.upperBounds);

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

			if (!(type instanceof InferenceVariable)) {
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

				/*
				 * Capture conversions...
				 */
				for (CaptureConversion captureConversion : boundSet.getCaptureConversions()) {
					Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);

					if (capturedArgument instanceof WildcardType)
						incorporateCapturedSupertype((WildcardType) capturedArgument, type);
				}

				/*
				 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
				 */
				for (InferenceVariable other : boundSet.getInferenceVariables()) {
					InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

					for (Type equality : new HashSet<>(otherBounds.equalities))
						if (equality != type && InferenceVariable.isProperType(equality))
							boundSet.getBoundsOnImpl(otherBounds.inferenceVariable).incorporateProperSupertypeSubstitution(equality,
									type, inferenceVariable);
				}
			}
		}
	}

	private void logBound(Type from, Type to, String boundString) {
		// System.out.println(System.identityHashCode(boundSet) + " " + from + " "
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
	public void incorporateProperEqualitySubstitution(Type U, InferenceVariable S, Type T) {
		if (!Types.getAllMentionedBy(T, inferenceVariable::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

			T = resolver.resolve(T);

			ConstraintFormula.reduce(Kind.EQUALITY, S, T, boundSet);
		}
	}

	/*
	 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
	 */
	public void incorporateProperSubtypeSubstitution(Type U, InferenceVariable S, Type T) {
		if (!Types.getAllMentionedBy(T, inferenceVariable::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

			T = resolver.resolve(T);

			ConstraintFormula.reduce(Kind.SUBTYPE, S, T, boundSet);
		}
	}

	public void incorporateProperSupertypeSubstitution(Type U, Type S, InferenceVariable T) {
		if (!Types.getAllMentionedBy(S, inferenceVariable::equals).isEmpty()) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

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
						Type supertypeS = ParameterizedTypes.resolveSupertypeParameters(S, type);
						Type supertypeT = ParameterizedTypes.resolveSupertypeParameters(T, type);

						for (TypeVariable<?> parameter : ParameterizedTypes.getAllTypeParameters(type)) {
							Type argumentS = ParameterizedTypes.getAllTypeArgumentsMap((ParameterizedType) supertypeS).get(parameter);
							Type argumentT = ParameterizedTypes.getAllTypeArgumentsMap((ParameterizedType) supertypeT).get(parameter);

							if (!(argumentS instanceof WildcardType) && !(argumentT instanceof WildcardType))
								ConstraintFormula.reduce(Kind.EQUALITY, argumentS, argumentT, boundSet);
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
			boundSet.incorporate().falsehood("Cannot incorporate equality: " + this);
	}

	public void incorporateCapturedSubtype(CaptureConversion c, WildcardType A, TypeVariable<?> P, Type R) {
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
				ConstraintFormula.reduce(Kind.SUBTYPE, θ.resolve(IntersectionType.from(B)), R, boundSet);
			}

			if (B.length == 0 || (B.length == 1 && B[0].equals(Object.class))) {
				/*
				 * If Bi is Object, then αi <: R implies the constraint formula ‹T <: R›
				 */
				ConstraintFormula.reduce(Kind.SUBTYPE, IntersectionType.from(T), R, boundSet);
			}
		} else if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE, θ.resolve(IntersectionType.from(B)), R, boundSet);
		} else {
			/*
			 * If Ai is a wildcard of the form ?:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE, θ.resolve(IntersectionType.from(B)), R, boundSet);
		}
	}

	public void incorporateCapturedSupertype(WildcardType A, Type R) {
		if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * R <: αi implies the constraint formula ‹R <: T›
			 */
			ConstraintFormula.reduce(Kind.SUBTYPE, R, IntersectionType.from(A.getLowerBounds()), boundSet);
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
			boundSet.incorporate().falsehood("Cannot incorporate supertype: " + this);
		}
	}
}
