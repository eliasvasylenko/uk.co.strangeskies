package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class UnmodifiableObservableList<S extends UnmodifiableObservableList<S, E>, E>
		implements ListDecorator<E>, ObservableList<S, E> {
	static class UnmodifiableObservableListImpl<E>
			extends UnmodifiableObservableList<UnmodifiableObservableListImpl<E>, E> {
		UnmodifiableObservableListImpl(ObservableList<?, ? extends E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableListImpl<E> copy() {
			return new UnmodifiableObservableListImpl<>(((ObservableList<?, E>) getComponent()).copy());
		}
	}

	private final ObservableList<?, ? extends E> component;

	private final ObservableImpl<S> observable;
	private final Consumer<ObservableList<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<? super Change<? extends E>> changeObserver;

	@SuppressWarnings("unchecked")
	protected UnmodifiableObservableList(ObservableList<?, ? extends E> component) {
		this.component = component;

		observable = new ObservableImpl<>();
		observer = l -> observable.fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<>();
		changeObserver = c -> changes.fire((Change<E>) c);
		component.changes().addWeakObserver(changeObserver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> getComponent() {
		return (List<E>) component;
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
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
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
