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
package uk.co.strangeskies.utilities;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

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
 * @param <R>
 *          the type we may assign from
 * 
 */
public class ObservablePropertyImpl<T extends R, R> implements ObservableProperty<T, R> {
	protected class ChangeImpl implements Change<T> {
		T previous;
		T current;

		@Override
		public T newValue() {
			if (currentChange == this) {
				synchronized (ObservablePropertyImpl.this) {
					if (currentChange == this) {
						currentChange = null;
					}
				}
			}
			return current;
		}

		@Override
		public T previousValue() {
			return previous;
		}
	}

	private T value;
	private final BiFunction<R, T, T> assignmentFunction;
	private final BiPredicate<T, T> equality;
	private final Set<Consumer<? super T>> observers = new LinkedHashSet<>();
	private final ObservableImpl<Change<T>> changeObservable = new ObservableImpl<>();
	private ChangeImpl currentChange;

	protected ObservablePropertyImpl(BiFunction<R, T, T> assignmentFunction, BiPredicate<T, T> equality, T initialValue) {
		this.assignmentFunction = assignmentFunction;
		this.equality = equality;
		value = initialValue;
	}

	@Override
	public boolean addObserver(Consumer<? super T> observer) {
		return observers.add(observer);
	}

	@Override
	public boolean removeExactObserver(Consumer<? super T> observer) {
		return observers.remove(observer);
	}

	@Override
	public Observable<Change<T>> changes() {
		return changeObservable;
	}

	/**
	 * Remove all observers from forwarding.
	 */
	public void clearObservers() {
		observers.clear();
	}

	/**
	 * Fire the given message to all observers.
	 * 
	 * @param value
	 *          the message event to send
	 */
	@Override
	public synchronized T set(R value) {
		T previous = this.value;
		this.value = assignmentFunction.apply(value, this.value);

		if (!equality.test(this.value, previous)) {
			for (Consumer<? super T> listener : new ArrayList<>(observers)) {
				listener.accept(this.value);
			}

			ChangeImpl currentChange = this.currentChange;
			if (currentChange == null) {
				this.currentChange = new ChangeImpl();
				this.currentChange.previous = previous;
				changeObservable.fire(this.currentChange);
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

	/**
	 * @return a list of all observers attached to this observable
	 */
	public Set<Consumer<? super T>> getObservers() {
		return observers;
	}
}
