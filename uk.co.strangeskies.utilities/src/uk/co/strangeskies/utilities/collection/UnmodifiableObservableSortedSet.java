package uk.co.strangeskies.utilities.collection;

import java.util.SortedSet;

public abstract class UnmodifiableObservableSortedSet<S extends UnmodifiableObservableSortedSet<S, E>, E>
		extends UnmodifiableObservableSet<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class UnmodifiableObservableSortedSetImpl<E>
			extends UnmodifiableObservableSortedSet<UnmodifiableObservableSortedSetImpl<E>, E> {
		UnmodifiableObservableSortedSetImpl(ObservableSortedSet<?, ? extends E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableSortedSetImpl<E> copy() {
			return new UnmodifiableObservableSortedSetImpl<>(((ObservableSortedSet<?, E>) getComponent()).copy());
		}
	}

	protected UnmodifiableObservableSortedSet(ObservableSortedSet<?, ? extends E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
