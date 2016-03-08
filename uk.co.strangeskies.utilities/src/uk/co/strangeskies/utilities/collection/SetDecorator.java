/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface SetDecorator<E> extends CollectionDecorator<Set<E>, E>, Set<E> {
	@Override
	Set<E> getComponent();

	@Override
	default boolean add(E e) {
		return CollectionDecorator.super.add(e);
	}

	@Override
	default boolean addAll(Collection<? extends E> c) {
		return CollectionDecorator.super.addAll(c);
	}

	@Override
	default void clear() {
		CollectionDecorator.super.clear();
	}

	@Override
	default boolean contains(Object o) {
		return CollectionDecorator.super.contains(o);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return CollectionDecorator.super.containsAll(c);
	}

	@Override
	default boolean isEmpty() {
		return CollectionDecorator.super.isEmpty();
	}

	@Override
	default Iterator<E> iterator() {
		return CollectionDecorator.super.iterator();
	}

	@Override
	default boolean remove(Object o) {
		return CollectionDecorator.super.remove(o);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return CollectionDecorator.super.removeAll(c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return CollectionDecorator.super.retainAll(c);
	}

	@Override
	default int size() {
		return CollectionDecorator.super.size();
	}

	@Override
	default Object[] toArray() {
		return CollectionDecorator.super.toArray();
	}

	@Override
	default <A> A[] toArray(A[] a) {
		return CollectionDecorator.super.toArray(a);
	}
}
