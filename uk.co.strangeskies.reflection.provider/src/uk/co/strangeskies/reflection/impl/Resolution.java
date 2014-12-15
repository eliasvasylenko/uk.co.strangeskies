package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.reflection.impl.Bound.BoundVisitor;
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

	private void calculateRemainingDependencies() {
		for (InferenceVariable inferenceVariable : instantiations.keySet()) {
			remainingDependencies.add(inferenceVariable, inferenceVariable);
		}
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
		public void acceptCaptureConversion(Map<Type, TypeVariable<?>> c) {}
	}
}
