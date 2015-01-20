/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection.decorator;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import uk.co.strangeskies.utilities.Property;

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
