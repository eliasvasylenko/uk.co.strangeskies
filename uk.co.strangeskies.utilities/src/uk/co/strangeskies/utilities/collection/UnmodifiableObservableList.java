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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.utilities.ObservableImpl;

public abstract class UnmodifiableObservableList<S extends ObservableList<S, E>, E> extends ObservableImpl<S>
		implements ListDecorator<E>, ObservableList<S, E> {
	static class UnmodifiableObservableListImpl<E>
			extends UnmodifiableObservableList<UnmodifiableObservableListImpl<E>, E> {
		UnmodifiableObservableListImpl(ObservableList<?, ? extends E> component) {
			super(component);
		}

		@SuppressWarnings("unchecked")
		@Override
		public UnmodifiableObservableListImpl<E> copy() {
			return new UnmodifiableObservableListImpl<>(((ObservableList<?, E>) getComponent()).copy());
		}
	}

	private final List<E> component;
	private final List<E> silentComponent;

	private final Consumer<ObservableList<?, ? extends E>> observer;
	private final ObservableImpl<Change<E>> changes;
	private final Consumer<? super Change<? extends E>> changeObserver;

	@SuppressWarnings("unchecked")
	protected UnmodifiableObservableList(ObservableList<?, ? extends E> component) {
		this.component = Collections.unmodifiableList(component);
		silentComponent = Collections.unmodifiableList(component.silent());

		observer = l -> fire(getThis());
		component.addWeakObserver(observer);

		changes = new ObservableImpl<>();
		changeObserver = c -> changes.fire((Change<E>) c);
		component.changes().addWeakObserver(changeObserver);
	}

	@Override
	public List<E> getComponent() {
		return component;
	}

	@Override
	public ObservableImpl<Change<E>> changes() {
		return changes;
	}

	@Override
	public List<E> silent() {
		return silentComponent;
	}
}
