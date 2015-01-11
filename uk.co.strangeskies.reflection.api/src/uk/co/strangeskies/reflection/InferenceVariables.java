package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.BoundVisitor.PartialBoundVisitor;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

public class InferenceVariables {
	private final Map<GenericTypeContainer<?>, Map<TypeVariable<?>, InferenceVariable<?>>> inferenceVariables;
	private final Map<InferenceVariable<?>, Type> instantiations;
	private final MultiMap<InferenceVariable<?>, InferenceVariable<?>, ? extends Set<InferenceVariable<?>>> remainingDependencies;

	public InferenceVariables() {
		inferenceVariables = new TreeMap<>(
				new IdentityComparator<GenericTypeContainer<?>>());
		instantiations = new HashMap<>();
		remainingDependencies = new MultiHashMap<>(HashSet::new);
	}

	@Override
	public String toString() {
		return inferenceVariables.toString();
	}

	public boolean contains(GenericTypeContainer<?> context,
			TypeVariable<?> variable) {
		return inferenceVariables.get(context).containsKey(variable);
	}

	public InferenceVariable<?> get(GenericTypeContainer<?> context,
			TypeVariable<?> variable) {
		return inferenceVariables.get(context).get(variable);
	}

	public Set<InferenceVariable<?>> get(GenericTypeContainer<?> context,
			Collection<? extends TypeVariable<?>> variables) {
		HashSet<InferenceVariable<?>> inferenceVariables = new HashSet<>();
		for (TypeVariable<?> variable : variables)
			inferenceVariables
					.add(this.inferenceVariables.get(context).get(variable));
		return inferenceVariables;
	}

	public Set<InferenceVariable<?>> get(GenericTypeContainer<?> context) {
		return new HashSet<>(inferenceVariables.get(context).values());
	}

	public Set<InferenceVariable<?>> all() {
		return inferenceVariables.values().stream().map(Map::values)
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public boolean hasContext(GenericTypeContainer<?> context) {
		return inferenceVariables.get(context) != null;
	}

	public void addContext(GenericTypeContainer<?> context) {
		inferenceVariables.put(context, new HashMap<>());
	}

	public void add(GenericTypeContainer<?> context,
			TypeVariable<?> typeVariable, InferenceVariable<?> inferenceVariable) {
		inferenceVariables.get(context).put(typeVariable, inferenceVariable);
	}

	private void addRemainingDependency(InferenceVariable<?> variable,
			InferenceVariable<?> dependency) {
		/*
		 * An inference variable α depends on the resolution of an inference
		 * variable β if there exists an inference variable γ such that α depends on
		 * the resolution of γ and γ depends on the resolution of β.
		 */
		if (remainingDependencies.add(variable, dependency)) {
			for (InferenceVariable<?> transientDependency : remainingDependencies
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

	void recalculateRemainingDependencies(BoundSet bounds) {
		Set<InferenceVariable<?>> leftOfCapture = new HashSet<>();

		Set<InferenceVariable<?>> inferenceVariables = all();
		inferenceVariables.removeAll(instantiations.keySet());

		/*
		 * An inference variable α depends on the resolution of itself.
		 */
		for (InferenceVariable<?> inferenceVariable : inferenceVariables)
			addRemainingDependency(inferenceVariable, inferenceVariable);

		/*
		 * An inference variable α appearing on the left-hand side of a bound of the
		 * form G<..., α, ...> = capture(G<...>) depends on the resolution of every
		 * other inference variable mentioned in this bound (on both sides of the =
		 * sign).
		 */
		bounds.stream().forEach(b -> b.accept(new PartialBoundVisitor() {
			@Override
			public void acceptCaptureConversion(Map<Type, InferenceVariable<?>> c) {
				for (InferenceVariable<?> variable : c.values()) {
					if (inferenceVariables.contains(variable)) {
						for (InferenceVariable<?> dependency : c.values())
							if (inferenceVariables.contains(dependency))
								addRemainingDependency(variable, dependency);
						for (Type inC : c.keySet())
							for (InferenceVariable<?> dependency : InferenceVariable
									.getAllMentionedBy(inC))
								if (inferenceVariables.contains(dependency))
									addRemainingDependency(variable, dependency);
					}
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
			public void acceptEquality(InferenceVariable<?> a, InferenceVariable<?> b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptEquality(InferenceVariable<?> a, Type b) {
				for (InferenceVariable<?> inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			/*
			 * α <: T, T <: α
			 */
			@Override
			public void acceptSubtype(InferenceVariable<?> a, InferenceVariable<?> b) {
				assessDependency(a, b);
				assessDependency(b, a);
			}

			@Override
			public void acceptSubtype(InferenceVariable<?> a, Type b) {
				for (InferenceVariable<?> inB : InferenceVariable.getAllMentionedBy(b))
					assessDependency(a, inB);
			}

			@Override
			public void acceptSubtype(Type a, InferenceVariable<?> b) {
				for (InferenceVariable<?> inA : InferenceVariable.getAllMentionedBy(a))
					assessDependency(inA, b);
			}

			/*
			 * If α appears on the left-hand side of another bound of the form G<...,
			 * α, ...> = capture(G<...>), then β depends on the resolution of α.
			 * Otherwise, α depends on the resolution of β.
			 */
			public void assessDependency(InferenceVariable<?> a,
					InferenceVariable<?>... b) {
				if (leftOfCapture.contains(a)) {
					for (InferenceVariable<?> bItem : b)
						if (inferenceVariables.contains(bItem))
							addRemainingDependency(bItem, a);
				} else {
					for (InferenceVariable<?> bItem : b)
						if (inferenceVariables.contains(bItem))
							addRemainingDependency(a, bItem);
				}
			}
		}));
	}

	public void instantiate(Map<InferenceVariable<?>, Type> instantiations) {
		for (Map.Entry<InferenceVariable<?>, Type> instantiation : instantiations
				.entrySet()) {
			this.instantiations.put(instantiation.getKey(), instantiation.getValue());
		}
		remainingDependencies.keySet().removeAll(instantiations.keySet());
		remainingDependencies.removeAllFromAll(instantiations.keySet());
	}

	public void failInstantiations(Set<InferenceVariable<?>> minimalSet) {
		for (InferenceVariable<?> variable : minimalSet)
			instantiations.put(variable, null);
	}

	public Type getInstantiation(GenericTypeContainer<?> context,
			TypeVariable<?> variable) {
		return instantiations.get(get(context, variable));
	}

	public Type getInstantiation(InferenceVariable<?> variable) {
		return instantiations.get(variable);
	}

	public Set<InferenceVariable<?>> remainingDependencies(
			InferenceVariable<?> variable) {
		return remainingDependencies.get(variable);
	}

	public boolean isInstantiated(InferenceVariable<?> variable) {
		return instantiations.containsKey(variable);
	}
}
