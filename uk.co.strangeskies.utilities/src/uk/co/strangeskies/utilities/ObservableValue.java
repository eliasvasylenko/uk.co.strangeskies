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
package uk.co.strangeskies.utilities;

import java.util.function.Consumer;

/**
 * A value which can be {@link #get() fetched} and observed for updates and
 * {@link #changes() changes}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value
 */
public interface ObservableValue<T> extends Observable<T> {
	/**
	 * A value change event.
	 * 
	 * @author Elias N Vasylenko
	 *
	 * @param <T>
	 *          the type of the value
	 */
	interface Change<T> {
		T newValue();

		T previousValue();
	}

	/**
	 * @return the current value
	 */
	T get();

	/**
	 * @return an observable over changes to the value
	 */
	Observable<Change<T>> changes();

	/**
	 * @param <T>
	 *          the type of the immutable value
	 * @param value
	 *          the immutable value to create an observable over
	 * @return an observable over the given value which never changes or fires
	 *         events
	 */
	static <T> ObservableValue<T> immutable(T value) {
		return (ImmutableObservableValue<T>) () -> value;
	}
}

interface ImmutableObservableValue<T> extends ObservableValue<T> {
	@Override
	default boolean addObserver(Consumer<? super T> observer) {
		return true;
	}

	@Override
	default boolean removeObserver(Consumer<? super T> observer) {
		return true;
	}

	@Override
	default Observable<Change<T>> changes() {
		return Observable.immutable();
	}
}
