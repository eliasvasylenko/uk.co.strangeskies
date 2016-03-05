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
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public interface ObservableSet<S extends ObservableSet<S, E>, E>
		extends Set<E>, ObservableCollection<S, E, ObservableSet.Change<E>> {
	interface Change<E> {
		enum Type {
			ADDED, REMOVED
		}

		Type type();

		E element();
	}

	static <E> UnmodifiableObservableSet<E> unmodifiable(ObservableSet<?, ? extends E> set) {
		return new UnmodifiableObservableSet<>(set);
	}

	class UnmodifiableObservableSet<E> extends SetDecorator<E> implements ObservableSet<UnmodifiableObservableSet<E>, E> {
		private ObservableImpl<UnmodifiableObservableSet<E>> observable;

		@SuppressWarnings("unchecked")
		private UnmodifiableObservableSet(ObservableSet<?, ? extends E> component) {
			super((Set<E>) component);

			observable = new ObservableImpl<>();
			component.addObserver(s -> observable.fire(this));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Observable<Change<E>> changes() {
			return ((ObservableSet<?, E>) getComponent()).changes();
		}

		@Override
		public boolean addObserver(Consumer<? super UnmodifiableObservableSet<E>> observer) {
			return observable.addObserver(observer);
		}

		@Override
		public boolean removeObserver(Consumer<? super UnmodifiableObservableSet<E>> observer) {
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
		public boolean remove(Object o) {
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
			Iterator<E> base = super.iterator();
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
}
