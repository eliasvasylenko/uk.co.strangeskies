package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.reflection.impl.Bound.BoundVisitor;
import uk.co.strangeskies.reflection.impl.Bound.PartialBoundVisitor;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class Resolution {
	private final BoundSet bounds;
	private final Map<InferenceVariable, Type> instantiations;
	private final MultiMap<InferenceVariable, InferenceVariable, ? extends Set<?>> remainingDependencies;

	public Resolution(BoundSet bounds,
			Collection<InferenceVariable> inferenceVariables) {
		this.bounds = bounds;
		instantiations = new HashMap<>();
		inferenceVariables.stream().forEach(i -> instantiations.put(i, null));

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
		for (InferenceVariable inferenceVariable : instantiations.keySet())
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

	public boolean isComplete() {
		return false; // TODO
	}

	public boolean verify() {
		Property<Boolean, Boolean> verified = new IdentityProperty<Boolean>(true);
		BoundVerifier verifier = new BoundVerifier(verified::set);
		return bounds.stream().allMatch(b -> {
			b.accept(verifier);
			return verified.get();
		});
	}

	private class BoundVerifier implements BoundVisitor {
		private TypeResolver getResolver() {
			TypeResolver resolver = new TypeResolver();

			for (InferenceVariable var : instantiations.keySet())
				resolver = resolver.where(var, instantiations.get(var));

			return resolver;
		}

		private final Consumer<Boolean> result;

		public BoundVerifier(Consumer<Boolean> result) {
			this.result = result;
		}

		@Override
		public void acceptEquality(InferenceVariable a, InferenceVariable b) {
			result.accept(TypeToken.of(instantiations.get(a)).equals(
					TypeToken.of(instantiations.get(b))));
		}

		@Override
		public void acceptEquality(InferenceVariable a, Type b) {
			result.accept(TypeToken.of(instantiations.get(a)).equals(
					TypeToken.of(getResolver().resolveType(b))));
		}

		@Override
		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			result.accept(TypeToken.of(instantiations.get(b)).isAssignableFrom(
					TypeToken.of(instantiations.get(a))));
		}

		@Override
		public void acceptSubtype(InferenceVariable a, Type b) {
			result.accept(TypeToken.of(getResolver().resolveType(b))
					.isAssignableFrom(TypeToken.of(instantiations.get(a))));
		}

		@Override
		public void acceptSubtype(Type a, InferenceVariable b) {
			result.accept(TypeToken.of(instantiations.get(b)).isAssignableFrom(
					TypeToken.of(getResolver().resolveType(a))));
		}

		@Override
		public void acceptFalsehood() {
			result.accept(false);
		}

		@Override
		public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {}
	}
}
