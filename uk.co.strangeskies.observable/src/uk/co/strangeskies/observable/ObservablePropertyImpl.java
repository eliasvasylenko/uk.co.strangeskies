/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A simple implementation of {@link ObservableProperty} which maintains a list
 * of listeners to receive change events fired with {@link #set(Object)}.
 * <p>
 * Addition and removal of observers, as well as the firing of events, are
 * synchronized on the implementation object.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of event message to produce
 */
public class ObservablePropertyImpl<T> extends HotObservable<T> implements ObservableProperty<T> {
	protected class ChangeImpl implements Change<T> {
		T previous;
		T current;

		@Override
		public Optional<T> newValue() {
			if (currentChange == this) {
				synchronized (ObservablePropertyImpl.this) {
					if (currentChange == this) {
						currentChange = null;
					}
				}
			}
			return Optional.ofNullable(current);
		}

		@Override
		public Optional<T> previousValue() {
			return Optional.ofNullable(previous);
		}

		@Override
		public T tryNewValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T tryPreviousValue() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private T value;
	private final BiFunction<T, T, T> assignmentFunction;
	private final BiPredicate<T, T> equality;
	private final HotObservable<Change<T>> changeObservable = new HotObservable<>();
	private ChangeImpl currentChange;

	protected ObservablePropertyImpl(
			BiFunction<T, T, T> assignmentFunction,
			BiPredicate<T, T> equality,
			T initialValue) {
		this.assignmentFunction = assignmentFunction;
		this.equality = equality;
		value = initialValue;
	}

	@Override
	public Observable<Change<T>> changes() {
		return changeObservable;
	}

	@Override
	public void sendNext(T item) {
		set(item);
	}

	/**
	 * Fire the given message to all observers.
	 * 
	 * @param value
	 *          the message event to send
	 */
	@Override
	public synchronized T set(T value) {
		T previous = this.value;
		this.value = assignmentFunction.apply(value, this.value);

		if (!equality.test(this.value, previous)) {
			super.sendNext(this.value);

			ChangeImpl currentChange = this.currentChange;
			if (currentChange == null) {
				this.currentChange = new ChangeImpl();
				this.currentChange.previous = previous;
				changeObservable.sendNext(this.currentChange);
			} else {
				currentChange.current = this.value;
			}
		}

		return previous;
	}

	@Override
	public T get() {
		return value;
	}
}
