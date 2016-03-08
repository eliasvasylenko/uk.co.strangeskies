package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class SynchronizedObservableList<S extends SynchronizedObservableList<S, E>, E>
		implements ListDecorator<E>, ObservableList<S, E> {
	static class SynchronizedObservableListImpl<E>
			extends SynchronizedObservableList<SynchronizedObservableListImpl<E>, E> {
		SynchronizedObservableListImpl(ObservableList<?, E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SynchronizedObservableListImpl<E> copy() {
			return new SynchronizedObservableListImpl<>(((ObservableList<?, E>) getComponent()).copy());
		}
	}

	private final List<E> component;

	private final ObservableImpl<S> observable;
	private final Consumer<ObservableList<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<Change<E>> changeObserver;

	protected SynchronizedObservableList(ObservableList<?, E> component) {
		this.component = component;

		observable = new ObservableImpl<>();
		observer = l -> observable.fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<Change<E>>() {
			@Override
			public boolean addObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableList.this) {
					return super.addObserver(observer);
				}
			}

			@Override
			public boolean removeObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableList.this) {
					return super.removeObserver(observer);
				}
			}
		};
		changeObserver = changes::fire;
		component.changes().addWeakObserver(changeObserver);
	}

	@Override
	public List<E> getComponent() {
		return component;
	}

	@Override
	public Observable<Change<E>> changes() {
		return changes;
	}

	@Override
	public synchronized boolean addObserver(Consumer<? super S> observer) {
		return observable.addObserver(observer);
	}

	@Override
	public synchronized boolean removeObserver(Consumer<? super S> observer) {
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
	public synchronized void add(int index, E element) {
		getComponent().add(index, element);
	}

	@Override
	public synchronized boolean addAll(int index, Collection<? extends E> c) {
		return getComponent().addAll(index, c);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return getComponent().remove(o);
	}

	@Override
	public synchronized E remove(int index) {
		return getComponent().remove(index);
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
		Iterator<E> base = ListDecorator.super.iterator();
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
