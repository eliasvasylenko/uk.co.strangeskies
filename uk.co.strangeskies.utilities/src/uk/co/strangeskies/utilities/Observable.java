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

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * Simple interface for an observable object, with methods to add and remove
 * observers expecting the applicable type of message.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The message type. This may be {@link Void} if no message need be
 *          sent.
 */
public interface Observable<M> {
	/**
	 * A weak observer is one which automatically removes itself from an
	 * observable once it is otherwise available for garbage collection.
	 * 
	 * @author Elias N Vasylenko
	 * @param <T>
	 */
	class WeakObserver<T> implements Consumer<T> {
		private final Observable<? extends T> observable;
		private final WeakReference<Consumer<T>> consumer;

		private WeakObserver(Observable<? extends T> observable,
				Consumer<T> consumer) {
			this.observable = observable;
			this.consumer = new WeakReference<>(consumer);
		}

		@Override
		public void accept(T t) {
			Consumer<T> component = consumer.get();
			if (component == null) {
				new Thread(() -> observable.removeObserver(this)).start();
			} else {
				component.accept(t);
			}
		}
	}

	/**
	 * Observers added will receive messages from this Observable. A weak observer
	 * will be removed automatically once it is otherwise available for garbage
	 * collection.
	 * 
	 * @param observer
	 *          An observer to add.
	 * @return True if the observer was successfully added, false otherwise.
	 */
	default boolean addWeakObserver(Consumer<? super M> observer) {
		return addObserver(new WeakObserver<>(this, observer));
	}

	/**
	 * Observers added will receive messages from this Observable.
	 * 
	 * @param observer
	 *          An observer to add.
	 * @return True if the observer was successfully added, false otherwise.
	 */
	boolean addObserver(Consumer<? super M> observer);

	/**
	 * Observers removed will no longer receive messages from this Observable.
	 * 
	 * @param observer
	 *          An observer to remove.
	 * @return True if the observer was successfully removed, false otherwise.
	 */
	boolean removeObserver(Consumer<? super M> observer);

	/**
	 * Remove all observers from this Observable.
	 */
	void clearObservers();
}
