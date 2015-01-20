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
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.utilities.Property;

public class FilteredSetDecorator<E> extends SetDecorator<E> {
	public interface Filter<E> {
		public boolean filter(E element);
	}

	private final Filter<E> filter;

	public FilteredSetDecorator(Filter<E> filter) {
		super(new HashSet<E>());

		this.filter = filter;
	}

	public FilteredSetDecorator(Set<E> component, Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	public FilteredSetDecorator(Property<Set<E>, ? super Set<E>> component,
			Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	@Override
	public boolean add(E e) {
		return filter.filter(e) && super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (E e : c)
			changed = add(e) || changed;

		return changed;
	}
}
