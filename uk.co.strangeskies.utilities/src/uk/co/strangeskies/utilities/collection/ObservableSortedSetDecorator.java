/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

import java.util.SortedSet;
import java.util.function.Function;

public abstract class ObservableSortedSetDecorator<S extends ObservableSortedSet<S, E>, E>
		extends ObservableSetDecorator<S, E> implements SortedSetDecorator<E>, ObservableSortedSet<S, E> {
	static class ObservableSortedSetDecoratorImpl<C extends SortedSet<E>, E>
			extends ObservableSortedSetDecorator<ObservableSortedSetDecoratorImpl<C, E>, E> {
		private Function<? super C, ? extends C> copy;

		ObservableSortedSetDecoratorImpl(C set, Function<? super C, ? extends C> copy) {
			super(set);
			this.copy = copy;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ObservableSortedSetDecoratorImpl<C, E> copy() {
			return new ObservableSortedSetDecoratorImpl<>(copy.apply((C) getComponent()), copy);
		}
	}

	protected ObservableSortedSetDecorator(SortedSet<E> component) {
		super(component);
	}

	@Override
	public SortedSet<E> getComponent() {
		return (SortedSet<E>) super.getComponent();
	}
}
