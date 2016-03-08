package uk.co.strangeskies.utilities.collection;

import java.util.Comparator;
import java.util.SortedSet;

public abstract class SynchronizedObservableSortedSet<S extends SynchronizedObservableSortedSet<S, E>, E>
		extends SynchronizedObservableSet<S, E> implements ObservableSortedSet<S, E> {
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

	SynchronizedObservableSortedSet(ObservableSet<?, E> component) {
		super(component);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Comparator<? super E> comparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E first() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E last() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableSortedSet<?, E> unmodifiableView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservableSortedSet<?, E> synchronizedView() {
		// TODO Auto-generated method stub
		return null;
	}
}
