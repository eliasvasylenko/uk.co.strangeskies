package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class UnmodifiableObservableSet<S extends UnmodifiableObservableSet<S, E>, E> extends SetDecorator<E>
		implements ObservableSet<S, E> {
	static class UnmodifiableObservableSetImpl<E> extends SynchronizedObservableSet<UnmodifiableObservableSetImpl<E>, E> {
		@SuppressWarnings("unchecked")
		UnmodifiableObservableSetImpl(ObservableSet<?, ? extends E> component) {
			super((ObservableSet<?, E>) component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableSetImpl<E> copy() {
			return new UnmodifiableObservableSetImpl<>(((ObservableSet<?, E>) getComponent()).copy());
		}
	}

	private final ObservableImpl<S> observable;
	private final Consumer<ObservableSet<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<? super Change<? extends E>> changeObserver;

	@SuppressWarnings("unchecked")
	UnmodifiableObservableSet(ObservableSet<?, ? extends E> component) {
		super((Set<E>) component);

		observable = new ObservableImpl<>();
		observer = l -> observable.fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<>();
		changeObserver = c -> changes.fire((Change<E>) c);
		component.changes().addWeakObserver(changeObserver);
	}

	@Override
	public Observable<Change<E>> changes() {
		return changes;
	}

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Consumer<? super S> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator() {
		Iterator<E> base = super.iterator();
		return new Iterator<E>() {
			@Override
			public boolean hasNext() {
				return base.hasNext();
			}

			@Override
			public E next() {
				return base.next();
			}
		};
	}
}
