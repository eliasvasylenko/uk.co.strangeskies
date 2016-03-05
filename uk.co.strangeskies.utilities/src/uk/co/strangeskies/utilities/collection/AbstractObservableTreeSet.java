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
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class AbstractObservableTreeSet<S extends AbstractObservableTreeSet<S, E>, E> extends TreeSet<E>
		implements ObservableSet<S, E> {
	private static final long serialVersionUID = 1L;

	private final ObservableImpl<Change<E>> changeObservable = new ObservableImpl<>();
	private final ObservableImpl<S> stateObservable = new ObservableImpl<>();

	public AbstractObservableTreeSet() {}

	public AbstractObservableTreeSet(Comparator<? super E> comparator) {
		super(comparator);
	}

	public AbstractObservableTreeSet(Collection<? extends E> c) {
		super(c);
	}

	public AbstractObservableTreeSet(SortedSet<E> s) {
		super(s);
	}

	@Override
	public boolean add(E e) {
		synchronized (changeObservable) {
			if (super.add(e)) {
				changeObservable.fire(new Change<E>() {
					@Override
					public Type type() {
						return Type.ADDED;
					}

					@Override
					public E element() {
						return e;
					}
				});
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean remove(Object o) {
		synchronized (changeObservable) {
			if (super.remove(o)) {
				changeObservable.fire(new Change<E>() {
					@Override
					public Type type() {
						return Type.REMOVED;
					}

					@SuppressWarnings("unchecked")
					@Override
					public E element() {
						return (E) o;
					}
				});
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		synchronized (changeObservable) {
			return super.addAll(c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (changeObservable) {
			return super.removeAll(c);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		synchronized (changeObservable) {
			return super.retainAll(c);
		}
	}

	@Override
	public Observable<Change<E>> changes() {
		return changeObservable;
	}

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		synchronized (changeObservable) {
			return stateObservable.addObserver(observer);
		}
	}

	@Override
	public boolean removeObserver(Consumer<? super S> observer) {
		synchronized (changeObservable) {
			return stateObservable.removeObserver(observer);
		}
	}
}
