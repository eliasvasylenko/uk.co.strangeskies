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

import java.util.SortedSet;

public abstract class SynchronizedObservableSortedSet<S extends ObservableSortedSet<S, E>, E>
		extends SynchronizedObservableSet<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class SynchronizedObservableSortedSetImpl<E>
			extends SynchronizedObservableSet<SynchronizedObservableSortedSetImpl<E>, E> {
		SynchronizedObservableSortedSetImpl(ObservableSortedSet<?, E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SynchronizedObservableSortedSetImpl<E> copy() {
			return new SynchronizedObservableSortedSetImpl<>(((ObservableSortedSet<?, E>) getComponent()).copy());
		}
	}

	protected SynchronizedObservableSortedSet(ObservableSet<?, E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
