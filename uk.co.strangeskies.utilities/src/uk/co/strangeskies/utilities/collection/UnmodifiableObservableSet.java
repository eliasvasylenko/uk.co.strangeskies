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

public abstract class UnmodifiableObservableSet<S extends ObservableSet<S, E>, E> extends ObservableImpl<S>
		implements SetDecorator<E>, ObservableSet<S, E> {
	static class UnmodifiableObservableSetImpl<E> extends UnmodifiableObservableSet<UnmodifiableObservableSetImpl<E>, E> {
		UnmodifiableObservableSetImpl(ObservableSet<?, ? extends E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableSetImpl<E> copy() {
			return new UnmodifiableObservableSetImpl<>(((ObservableSet<?, E>) getComponent()).copy());
		}
	}

	private final Set<E> component;
	private final Set<E> silentComponent;

	private final Consumer<ObservableSet<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<? super Change<? extends E>> changeObserver;

	@SuppressWarnings("unchecked")
	protected UnmodifiableObservableSet(ObservableSet<?, ? extends E> component) {
		this.component = Collections.unmodifiableSet(component);
		silentComponent = Collections.unmodifiableSet(component.silent());

		observer = l -> fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<>();
		changeObserver = c -> changes.fire((Change<E>) c);
		component.changes().addWeakObserver(changeObserver);
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
}
