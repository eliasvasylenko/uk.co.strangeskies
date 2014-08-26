package uk.co.strangeskies.gears.utilities.collection;

import java.util.Collection;
import java.util.Iterator;

import uk.co.strangeskies.gears.utilities.Decorator;
import uk.co.strangeskies.gears.utilities.Property;

public abstract class CollectionDecorator<T extends Collection<E>, E> extends
		Decorator<T> implements Collection<E> {
	public CollectionDecorator(T component) {
		super(component);
	}

	public CollectionDecorator(Property<T, ? super T> component) {
		super(component);
	}

	@Override
	public boolean add(E e) {
		return getComponent().add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return getComponent().addAll(c);
	}

	@Override
	public void clear() {
		getComponent().clear();
	}

	@Override
	public boolean contains(Object o) {
		return getComponent().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getComponent().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return getComponent().isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return getComponent().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return getComponent().remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return getComponent().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return getComponent().retainAll(c);
	}

	@Override
	public int size() {
		return getComponent().size();
	}

	@Override
	public Object[] toArray() {
		return getComponent().toArray();
	}

	@Override
	public <A> A[] toArray(A[] a) {
		return getComponent().toArray(a);
	}
}
