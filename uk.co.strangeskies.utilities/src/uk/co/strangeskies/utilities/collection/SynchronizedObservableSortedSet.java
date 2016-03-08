package uk.co.strangeskies.utilities.collection;

import java.util.SortedSet;

public abstract class SynchronizedObservableSortedSet<S extends SynchronizedObservableSortedSet<S, E>, E>
		extends SynchronizedObservableSet<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class SynchronizedObservableSortedSetImpl<E>
			extends SynchronizedObservableSet<SynchronizedObservableSortedSetImpl<E>, E> {
		SynchronizedObservableSortedSetImpl(ObservableSortedSet<?, E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SynchronizedObservableSortedSetImpl<E> copy() {
			return new SynchronizedObservableSortedSetImpl<>(((ObservableSortedSet<?, E>) getComponent()).copy());
		}
	}

	protected SynchronizedObservableSortedSet(ObservableSet<?, E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
