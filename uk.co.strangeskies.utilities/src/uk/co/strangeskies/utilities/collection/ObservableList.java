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
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public interface ObservableList<S extends ObservableList<S, E>, E>
		extends List<E>, ObservableCollection<S, E, ObservableList.Change<E>> {
	interface Change<E> {
		boolean next();

		void reset();

		int index();

		List<E> getRemoved();

		List<E> getAdded();
	}

	static <E> UnmodifiableObservableList<E> unmodifiable(ObservableList<?, ? extends E> set) {
		return new UnmodifiableObservableList<>(set);
	}

	class UnmodifiableObservableList<E> extends ListDecorator<E>
			implements ObservableList<UnmodifiableObservableList<E>, E> {
		private ObservableImpl<UnmodifiableObservableList<E>> observable;

		@SuppressWarnings("unchecked")
		private UnmodifiableObservableList(ObservableList<?, ? extends E> component) {
			super((List<E>) component);

			observable = new ObservableImpl<>();
			component.addObserver(s -> observable.fire(this));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Observable<Change<E>> changes() {
			return ((ObservableList<?, E>) getComponent()).changes();
		}

		@Override
		public boolean addObserver(Consumer<? super UnmodifiableObservableList<E>> observer) {
			return observable.addObserver(observer);
		}

		@Override
		public boolean removeObserver(Consumer<? super UnmodifiableObservableList<E>> observer) {
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

		@Override
		public UnmodifiableObservableList<E> copy() {
			return new UnmodifiableObservableList<>(this.copy());
		}
	}
}
