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

public abstract class SynchronizedObservableSet<S extends SynchronizedObservableSet<S, E>, E> extends ObservableImpl<S>
		implements SetDecorator<E>, ObservableSet<S, E> {
	static class SynchronizedObservableSetImpl<E> extends SynchronizedObservableSet<SynchronizedObservableSetImpl<E>, E> {
		SynchronizedObservableSetImpl(ObservableSet<?, E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SynchronizedObservableSetImpl<E> copy() {
			return new SynchronizedObservableSetImpl<>(((ObservableSet<?, E>) getComponent()).copy());
		}
	}

	private final Set<E> component;

	private final Consumer<ObservableSet<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<Change<E>> changeObserver;

	protected SynchronizedObservableSet(ObservableSet<?, E> component) {
		this.component = component;

		observer = l -> fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<Change<E>>() {
			@Override
			public boolean addObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableSet.this) {
					return super.addObserver(observer);
				}
			}

			@Override
			public boolean removeObserver(Consumer<? super Change<E>> observer) {
				synchronized (SynchronizedObservableSet.this) {
					return super.removeObserver(observer);
				}
			}
		};
		changeObserver = changes::fire;
		component.changes().addWeakObserver(changeObserver);
	}

	@Override
	public Set<E> getComponent() {
		return component;
	}

	@Override
	public Observable<Change<E>> changes() {
		return changes;
	}

	@Override
	public synchronized boolean add(E e) {
		return getComponent().add(e);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		return getComponent().addAll(c);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return getComponent().remove(o);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		return getComponent().removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		return getComponent().retainAll(c);
	}

	@Override
	public synchronized Iterator<E> iterator() {
		Iterator<E> base = SetDecorator.super.iterator();
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
	public String toString() {
		return getComponent().toString();
	}

	@Override
	public int hashCode() {
		return getComponent().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getComponent().equals(obj);
	}
}
