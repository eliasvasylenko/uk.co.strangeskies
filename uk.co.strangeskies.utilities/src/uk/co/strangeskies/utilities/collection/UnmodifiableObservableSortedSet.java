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

public abstract class UnmodifiableObservableSortedSet<S extends UnmodifiableObservableSortedSet<S, E>, E>
		extends UnmodifiableObservableSet<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class UnmodifiableObservableSortedSetImpl<E>
			extends UnmodifiableObservableSortedSet<UnmodifiableObservableSortedSetImpl<E>, E> {
		UnmodifiableObservableSortedSetImpl(ObservableSortedSet<?, ? extends E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableSortedSetImpl<E> copy() {
			return new UnmodifiableObservableSortedSetImpl<>(((ObservableSortedSet<?, E>) getComponent()).copy());
		}
	}

	protected UnmodifiableObservableSortedSet(ObservableSortedSet<?, ? extends E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
