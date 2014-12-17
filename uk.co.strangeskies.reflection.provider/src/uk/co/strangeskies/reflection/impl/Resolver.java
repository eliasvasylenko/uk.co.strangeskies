package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.RecursiveTypeVisitor;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.reflection.impl.Bound.PartialBoundVisitor;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

import com.google.common.reflect.TypeToken;

public class Resolver {
	private final BoundSet bounds;
	private final Set<InferenceVariable> inferenceVariables;

	private final Map<InferenceVariable, Type> instantiations;

	private final MultiMap<InferenceVariable, InferenceVariable, ? extends Set<InferenceVariable>> remainingDependencies;

	public Resolver(BoundSet bounds,
			Collection<InferenceVariable> inferenceVariables) {
		this.bounds = bounds;
		this.inferenceVariables = new HashSet<>(inferenceVariables);

		instantiations = new HashMap<>();

		remainingDependencies = new MultiHashMap<>(HashSet::new);
		calculateRemainingDependencies();
	}

	private void addRemainingDependency(InferenceVariable variable,
			InferenceVariable dependency) {
		/*
		 * An inference variable α depends on the resolution of an inference
		 * variable β if there exists an inference variable γ such that α depends on
		 * the resolution of γ and γ depends on the resolution of β.
		 */
		if (remainingDependencies.add(variable, dependency)) {
			for (InferenceVariable transientDependency : remainingDependencies
					.get(dependency))
				addRemainingDependency(variable, transientDependency);

			remainingDependencies
					.entrySet()
					.stream()
					.filter(e -> e.getValue().contains(variable))
					.map(Entry::getKey)
					.forEach(
							transientDependent -> addRemainingDependency(transientDependent,
									variable));
		}
	}

	private void calculateRemainingDependencies() {
		Set<InferenceVariable> leftOfCapture = new HashSet<>();

		/*
		 * An inference variable α depends on the resolution of itself.
		 */
		for (InferenceVariable inferenceVariable : inferenceVariables)
			addRemainingDependency(inferenceVariable, inferenceVariable);

		/*
		 * An inference variable α appearing on the left-hand side of a bound of the
		 * form G<..., α, ...> = capture(G<...>) depends on the resolution of every
		 * other inference variable mentioned in this bound (on both sides of the =
		 * sign).
		 */
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {
				for (InferenceVariable variable : c.values()) {
					for (InferenceVariable dependency : c.values())
						addRemainingDependency(variable, dependency);
					for (Type inC : c.keySet())
						for (InferenceVariable dependency : InferenceVariable
								.getAllMentionedBy(inC))
							addRemainingDependency(variable, dependency);
				}

				leftOfCapture.addAll(c.values());
			}
		}));

		/*
		 * Given a bound of one of the following forms, where T is either an
		 * inference variable β or a type that mentions β:
		 */
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			/*
			 * α = T, T = α
			 */
			@Override
			public void acceptEquality(InferenceVariable a, InferenceVariable b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptEquality(InferenceVariable a, Type b) {
				for (InferenceVariable inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			/*
			 * α <: T, T <: α
			 */
			@Override
			public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptSubtype(InferenceVariable a, Type b) {
				for (InferenceVariable inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			@Override
			public void acceptSubtype(Type a, InferenceVariable b) {
				for (InferenceVariable inA : InferenceVariable.getAllMentionedBy(a))
					assessDependency(inA, b);
			}

			/*
			 * If α appears on the left-hand side of another bound of the form G<...,
			 * α, ...> = capture(G<...>), then β depends on the resolution of α.
			 * Otherwise, α depends on the resolution of β.
			 */
			public void assessDependency(InferenceVariable a, InferenceVariable... b) {
				if (leftOfCapture.contains(a))
					for (InferenceVariable bItem : b)
						addRemainingDependency(bItem, a);
				else
					for (InferenceVariable bItem : b)
						addRemainingDependency(a, bItem);
			}
		}));
	}

	public boolean validate() {
		return validate(inferenceVariables);
	}

	public boolean validate(InferenceVariable... variable) {
		return validate(variable);
	}

	public boolean validate(Collection<? extends InferenceVariable> variable) {
		return resolve(variable).values().stream().allMatch(v -> v != null);
	}

	public Map<InferenceVariable, Type> resolve() {
		return resolve(inferenceVariables);
	}

	public Type resolve(InferenceVariable variable) {
		Type instantiation = instantiations.get(variable);

		if (instantiations.containsKey(variable))
			instantiation = instantiations.get(variable);
		else {
			resolveIndependentSet(remainingDependencies.get(instantiation));
			instantiation = instantiations.get(variable);
		}

		return instantiation;
	}

	public Map<InferenceVariable, Type> resolve(InferenceVariable... variables) {
		return resolve(Arrays.asList(variables));
	}

	public Map<InferenceVariable, Type> resolve(
			Collection<? extends InferenceVariable> variables) {
		/*
		 * Given a set of inference variables to resolve, let V be the union of this
		 * set and all variables upon which the resolution of at least one variable
		 * in this set depends.
		 */
		resolveIndependentSet(variables.stream()
				.filter(v -> !instantiations.containsKey(v))
				.map(remainingDependencies::get).flatMap(Set::stream)
				.collect(Collectors.toSet()));

		Map<InferenceVariable, Type> instantiations = new HashMap<>();

		for (InferenceVariable variable : variables)
			instantiations.put(variable, this.instantiations.get(variable));

		return instantiations;
	}

	private void resolveIndependentSet(Set<InferenceVariable> variables) {
		/*
		 * If every variable in V has an instantiation, then resolution succeeds and
		 * this procedure terminates.
		 */
		while (!variables.isEmpty()) {
			/*
			 * Otherwise, let { α1, ..., αn } be a non-empty subset of uninstantiated
			 * variables in V such that i) for all i (1 ≤ i ≤ n), if αi depends on the
			 * resolution of a variable β, then either β has an instantiation or there
			 * is some j such that β = αj; and ii) there exists no non-empty proper
			 * subset of { α1, ..., αn } with this property. Resolution proceeds by
			 * generating an instantiation for each of α1, ..., αn based on the bounds
			 * in the bound set:
			 */
			Set<InferenceVariable> minimalSet = new HashSet<>(variables);
			int minimalSetSize = variables.size();
			for (InferenceVariable variable : variables)
				if (remainingDependencies.get(variable).size() < minimalSetSize)
					minimalSetSize = (minimalSet = remainingDependencies.get(variable))
							.size();

			resolveMinimalIndepdendentSet(minimalSet);

			remainingDependencies.keySet().removeAll(minimalSet);
			remainingDependencies.removeAllFromAll(minimalSet);
			variables.removeAll(minimalSet);
		}
	}

	private void resolveMinimalIndepdendentSet(Set<InferenceVariable> minimalSet) {
		IdentityProperty<Boolean> containsCaptureConversion = new IdentityProperty<Boolean>(
				false);
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {
				if (c.values().stream().anyMatch(minimalSet::contains))
					containsCaptureConversion.set(true);
			};
		}));

		if (!containsCaptureConversion.get()) {
			System.out.println(" sfagfsdafsdgdf");
			/*
			 * If the bound set does not contain a bound of the form G<..., αi, ...> =
			 * capture(G<...>) for all i (1 ≤ i ≤ n), then a candidate instantiation
			 * Ti is defined for each αi:
			 */
			BoundSet bounds = new BoundSet(this.bounds);
			Map<InferenceVariable, Type> instantiationCandidates = new HashMap<>();

			try {
				for (InferenceVariable variable : minimalSet) {
					Set<Type> lowerBounds = new HashSet<>();
					Set<Type> upperBounds = new HashSet<>();
					IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(
							false);
					bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
						@Override
						public void acceptSubtype(Type a, InferenceVariable b) {
							if (InferenceVariable.isProperType(a))
								lowerBounds.add(a);
						};

						@Override
						public void acceptSubtype(InferenceVariable a, Type b) {
							if (InferenceVariable.isProperType(b))
								upperBounds.add(b);
						};
					}));
					Type instantiationCandidate;
					if (!lowerBounds.isEmpty()) {
						/*
						 * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti =
						 * lub(L1, ..., Lk) (§4.10.4).
						 */
						instantiationCandidate = leastUpperBound(lowerBounds);
					} else if (hasThrowableBounds.get()) {
						/*
						 * Otherwise, if the bound set contains throws αi, and the proper
						 * upper bounds of αi are, at most, Exception, Throwable, and
						 * Object, then Ti = RuntimeException.
						 */
						throw new AssertionError();
					} else {
						/*
						 * Otherwise, where αi has proper upper bounds U1, ..., Uk, Ti =
						 * glb(U1, ..., Uk) (§5.1.10).
						 */
						instantiationCandidate = greatestLowerBound(upperBounds);
					}
					instantiationCandidates.put(variable, instantiationCandidate);
					bounds.incorporate().acceptEquality(variable, instantiationCandidate);
				}
			} catch (TypeInferenceException e) {
				e.printStackTrace();
				instantiationCandidates = null;
			}

			if (instantiationCandidates != null) {
				for (Map.Entry<InferenceVariable, Type> instantiation : instantiationCandidates
						.entrySet()) {
					instantiations.put(instantiation.getKey(), instantiation.getValue());
					this.bounds.incorporate().acceptEquality(instantiation.getKey(),
							instantiation.getValue());
				}
				return;
			}
		}

		/*
		 * the bound set contains a bound of the form G<..., αi, ...> =
		 * capture(G<...>) for some i (1 ≤ i ≤ n), or;
		 * 
		 * If the bound set produced in the step above contains the bound false;
		 * 
		 * then let Y1, ..., Yn be fresh type variables whose bounds are as follows:
		 */
		for (InferenceVariable variable : minimalSet)
			instantiations.put(variable, null);
	}

	private Type leastUpperBound(Set<Type> lowerBounds) {
		if (lowerBounds.size() == 1)
			/*
			 * If k = 1, then the lub is the type itself: lub(U) = U.
			 */
			return lowerBounds.iterator().next();
		else {
			/*
			 * For each Ui (1 ≤ i ≤ k):
			 */

			Iterator<Type> lowerBoundsIterator = lowerBounds.iterator();
			MultiMap<Class<?>, Type, ? extends Set<Type>> erasedCandidates = new MultiHashMap<>(
					HashSet::new);
			erasedCandidates.addAll(getErasedSupertypes(lowerBoundsIterator.next()));

			while (lowerBoundsIterator.hasNext()) {
				Map<Class<?>, Type> erasedSupertypes = getErasedSupertypes(lowerBoundsIterator
						.next());
				erasedCandidates.keySet().retainAll(erasedSupertypes.keySet());
				for (Map.Entry<Class<?>, Type> erasedSupertype : erasedSupertypes
						.entrySet())
					if (erasedCandidates.containsKey(erasedSupertype.getKey()))
						erasedCandidates.add(erasedSupertype.getKey(),
								erasedSupertype.getValue());
			}

			return new IntersectionType(erasedCandidates.entrySet().stream()
					.map(e -> best(e.getKey(), e.getValue())).collect(Collectors.toSet()));
		}
	}

	private Type best(Class<?> rawClass, Set<Type> parametrisations) {
		if (rawClass.getTypeParameters().length == 0)
			return rawClass;

		List<TypeVariable<?>> typeVariables = new ArrayList<>();
		Class<?> enclosingClass = rawClass;
		do {
			typeVariables.addAll(Arrays.asList(enclosingClass.getTypeParameters()));
		} while ((enclosingClass = enclosingClass.getEnclosingClass()) != null);

		return null; // TODO
	}

	private Map<Class<?>, Type> getErasedSupertypes(Type of) {
		Map<Class<?>, Type> supertypes = new HashMap<>();

		new RecursiveTypeVisitor(true, false, false, false, false) {
			@Override
			protected void visitClass(Class<?> type) {
				supertypes.put(type, null);
				super.visitClass(type);
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				supertypes.put(TypeLiteral.of(type).rawClass(), type);
				super.visitParameterizedType(type);
			}
		}.visit(of);

		return supertypes;
	}

	private Type greatestLowerBound(Set<Type> upperBounds) {
		if (upperBounds.size() == 1)
			return upperBounds.iterator().next();
		else
			return validateGreatestLowerBound(new IntersectionType(upperBounds));
	}

	public IntersectionType validateGreatestLowerBound(
			IntersectionType intersectionType) {
		Type[] types = intersectionType.getTypes();

		for (int i = 0; i < types.length; i++)
			for (int j = i + 1; j < types.length; j++) {
				TypeToken<?> iToken = TypeToken.of(types[i]);
				TypeToken<?> jToken = TypeToken.of(types[j]);
				if (!iToken.isAssignableFrom(jToken)
						&& !jToken.isAssignableFrom(iToken))
					throw new TypeInferenceException("Type '" + intersectionType
							+ "' is not a valid greater lowest bound.");
			}
		return intersectionType;
	}
}
