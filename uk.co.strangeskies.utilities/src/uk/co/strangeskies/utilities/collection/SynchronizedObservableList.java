/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class SynchronizedObservableList<S extends ObservableList<S, E>, E> extends ObservableImpl<S>
		implements ListDecorator<E>, ObservableList<S, E> {
	static class SynchronizedObservableListImpl<E>
			extends SynchronizedObservableList<SynchronizedObservableListImpl<E>, E> {
		SynchronizedObservableListImpl(ObservableList<?, E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SynchronizedObservableListImpl<E> copy() {
			return new SynchronizedObservableListImpl<>(((ObservableList<?, E>) getComponent()).copy());
		}
	}

	private final List<E> component;
	private final List<E> silentComponent;

	private final Consumer<ObservableList<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<Change<E>> changeObserver;

	protected SynchronizedObservableList(ObservableList<?, E> component) {
		this.component = Collections.synchronizedList(component);
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
			public boolean removeObserver(Consumer<? super Change<E>> observer) {
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

	public static <E> SynchronizedObservableListImpl<E> over(ObservableList<?, E> component) {
		return new SynchronizedObservableListImpl<>(component);
	}

	public Object getMutex() {
		return component;
	}

	@Override
	public List<E> getComponent() {
		return component;
	}

	@Override
	public Observable<Change<E>> changes() {
		return changes;
	}

	@Override
	public List<E> silent() {
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
	public boolean removeObserver(Consumer<? super S> observer) {
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
