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
 * This file is part of uk.co.strangeskies.utility.
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
package uk.co.strangeskies.collection.observable;

import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableImpl;
import uk.co.strangeskies.observable.Observer;

/**
 * A buffer to decouple the delivery of events with their sequential
 * consumption, such that the event firing threads are not blocked by listeners.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of event message to consume
 * @param <U>
 *          The type of event message to produce
 */
public abstract class ForwardingListener<T, U> implements Observer<T>, Observable<U> {
	private final ObservableImpl<U> listeners;

	/**
	 * Initialize a buffering listener with an empty set of listeners.
	 */
	public ForwardingListener() {
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
	public abstract void notify(T item);

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
