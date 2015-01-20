/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection.decorator;

import java.util.Collection;
import java.util.Iterator;

import uk.co.strangeskies.utilities.Decorator;
import uk.co.strangeskies.utilities.Property;

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
