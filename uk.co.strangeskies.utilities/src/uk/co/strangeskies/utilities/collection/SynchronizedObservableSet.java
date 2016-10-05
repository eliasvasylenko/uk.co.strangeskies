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

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class SynchronizedObservableSet<S extends ObservableSet<S, E>, E> extends ObservableImpl<S>
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
	private final Set<E> silentComponent;

	private final Consumer<ObservableSet<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<Change<E>> changeObserver;

	protected SynchronizedObservableSet(ObservableSet<?, E> component) {
		this.component = Collections.synchronizedSet(component);
		silentComponent = component.silent();

		observer = l -> fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<Change<E>>() {
			@Override
			public boolean addObserver(Consumer<? super Change<E>> observer) {
				synchronized (getMutex()) {
					return super.addObserver(observer);
				}
			}

			@Override
			public boolean removeExactObserver(Consumer<? super Change<E>> observer) {
				synchronized (getMutex()) {
					return super.removeObserver(observer);
				}
			}

			@Override
			public void clearObservers() {
				synchronized (getMutex()) {
					super.clearObservers();
				}
			}

			@Override
			public void fire(Change<E> item) {
				synchronized (getMutex()) {
					super.fire(item);
				}
			}
		};
		changeObserver = changes::fire;
		component.changes().addWeakObserver(changeObserver);
	}

	public static <E> SynchronizedObservableSetImpl<E> over(ObservableSet<?, E> component) {
		return new SynchronizedObservableSetImpl<>(component);
	}

	public Object getMutex() {
		return component;
	}

	@Override
	public Set<E> getComponent() {
		return component;
	}

	@Override
	public ObservableImpl<Change<E>> changes() {
		return changes;
	}

	@Override
	public Set<E> silent() {
		return silentComponent;
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

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		synchronized (getMutex()) {
			return super.addObserver(observer);
		}
	}

	@Override
	public boolean removeExactObserver(Consumer<? super S> observer) {
		synchronized (getMutex()) {
			return super.removeObserver(observer);
		}
	}

	@Override
	public void clearObservers() {
		synchronized (getMutex()) {
			super.clearObservers();
		}
	}

	@Override
	public void fire(S item) {
		synchronized (getMutex()) {
			super.fire(item);
		}
	}
}
