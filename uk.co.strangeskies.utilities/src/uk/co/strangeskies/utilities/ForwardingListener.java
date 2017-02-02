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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A buffer to decouple the delivery of events with their sequential
 * consumption, such that the event firing threads are not blocked by listeners.
 * <p>
 * Consumed events are translated to produced events by way of an implementation
 * of {@link Buffer}, the implemented methods of which are guaranteed to be
 * invoked synchronously.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of event message to consume
 * @param <U>
 *          The type of event message to produce
 */
public abstract class ForwardingListener<T, U> implements Observer<T>, Observable<U> {
	/**
	 * An interface for provision of a buffering strategy.
	 *
	 * @author Elias N Vasylenko
	 * @param <T>
	 *          The type of event to consume
	 * @param <U>
	 *          The type of event to produce
	 */
	public interface Buffer<T, U> {
		/**
		 * @return True if the buffer contains data which can be usefully forwarded
		 *         to a listener, false otherwise
		 */
		boolean isReady();

		/**
		 * @return Provide the next piece of buffered data ready for consumption
		 */
		U get();

		/**
		 * @param item
		 *          Buffer another piece of data for provision
		 */
		void put(T item);
	}

	private final Consumer<T> consumer;
	private final ObservableImpl<U> listeners;

	/**
	 * Initialize a buffering listener with an empty queue and an empty set of
	 * listeners.
	 * 
	 * @param consumer
	 *          The buffering strategy by which to provide consumed events
	 */
	public ForwardingListener(Function<Consumer<U>, Consumer<T>> consumer) {
		this.consumer = consumer.apply(this::fire);
		this.listeners = new ObservableImpl<>();
	}

	protected void fire(U item) {
		listeners.fire(item);
	}

	/**
	 * Fire an event.
	 * 
	 * @param item
	 *          The event data
	 */
	@Override
	public void notify(T item) {
		consumer.accept(item);
	}

	@Override
	public synchronized boolean addObserver(Observer<? super U> listener) {
		return listeners.addObserver(listener);
	}

	@Override
	public synchronized boolean removeObserver(Observer<? super U> listener) {
		return listeners.removeObserver(listener);
	}

	/**
	 * Remove all observers from forwarding.
	 */
	public synchronized void clearObservers() {
		listeners.clearObservers();
	}
}
