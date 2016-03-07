package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public class SynchronizedObservableSet<E> extends SetDecorator<E>
		implements ObservableSet<SynchronizedObservableSet<E>, E> {
	private final ObservableImpl<SynchronizedObservableSet<E>> observable;
	private final Consumer<ObservableSet<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<Change<E>> changeObserver;

	SynchronizedObservableSet(ObservableSet<?, E> component) {
		super(component);

		observable = new ObservableImpl<>();
		observer = l -> observable.fire(this);
		component.addWeakObserver(observer);

		changes = new ObservableImpl<Change<E>>() {
			@Override
			public boolean addObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableSet.this) {
					return super.addObserver(observer);
				}
			}

			@Override
			public boolean removeObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableSet.this) {
					return super.removeObserver(observer);
				}
			}
		};
		changeObserver = changes::fire;
		component.changes().addWeakObserver(changeObserver);
	}

	@Override
	public Observable<Change<E>> changes() {
		return changes;
	}

	@Override
	public synchronized boolean addObserver(Consumer<? super SynchronizedObservableSet<E>> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public synchronized boolean removeObserver(Consumer<? super SynchronizedObservableSet<E>> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public synchronized boolean add(E e) {
		return getComponent().add(e);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		return getComponent().addAll(c);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return getComponent().remove(o);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		return getComponent().removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		return getComponent().retainAll(c);
	}

	@Override
	public synchronized Iterator<E> iterator() {
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

	@SuppressWarnings("unchecked")
	@Override
	public SynchronizedObservableSet<E> copy() {
		return new SynchronizedObservableSet<>(((ObservableSet<?, E>) getComponent()).copy());
	}
}
