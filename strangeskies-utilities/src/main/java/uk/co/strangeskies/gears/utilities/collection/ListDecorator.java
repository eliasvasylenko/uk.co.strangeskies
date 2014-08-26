package uk.co.strangeskies.gears.utilities.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import uk.co.strangeskies.gears.utilities.Property;

public class ListDecorator<E> extends CollectionDecorator<List<E>, E> implements
		List<E> {
	public ListDecorator(List<E> component) {
		super(component);
	}

	public ListDecorator(Property<List<E>, ? super List<E>> component) {
		super(component);
	}

	@Override
	public void add(int index, E element) {
		getComponent().add(index, element);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return getComponent().addAll(index, c);
	}

	@Override
	public E get(int index) {
		return getComponent().get(index);
	}

	@Override
	public int indexOf(Object o) {
		return getComponent().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getComponent().lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return getComponent().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return getComponent().listIterator(index);
	}

	@Override
	public E remove(int index) {
		return getComponent().remove(index);
	}

	@Override
	public E set(int index, E element) {
		return getComponent().set(index, element);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return getComponent().subList(fromIndex, toIndex);
	}
}
