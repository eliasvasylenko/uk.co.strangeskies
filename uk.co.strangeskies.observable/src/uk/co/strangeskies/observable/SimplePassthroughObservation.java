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

/**
 * This is a helper class for implementing {@link Observable passive
 * observables}.
 * <p>
 * A passive observable is one which does not maintain a set of observations or
 * manage its own events, instead deferring to one or more parents. When an
 * observer subscribes to a passive observable, typically the observer is
 * decorated, and the decorator is then subscribed to the parents. This way the
 * decorator can modify, inspect, or filter events as appropriate before passing
 * them back through to the original observer.
 * <p>
 * This class is a partial implementation of such a decorator, taking care of
 * the subscription process and providing default event handling implementations
 * which simply pass the events along without modification.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The message type of the parent observables
 */
public class SimplePassthroughObservation<T> extends PassthroughObservation<T, T> {
	private final Observer<? super T> observer;
	private final Observer<T> actions;

	public SimplePassthroughObservation(
			Observable<? extends T> parentObservable,
			Observer<? super T> observer,
			Observer<T> actions) {
		this.observer = observer;
		this.actions = actions;

		passthroughObservation(parentObservable);
	}

	@Override
	public void onNext(T message) {
		actions.onNext(message);
		observer.onNext(message);
	}

	@Override
	public void onObserve() {
		actions.onObserve(this);
		observer.onObserve(this);
	}

	@Override
	public void onComplete() {
		actions.onComplete();
		observer.onComplete();
	}

	@Override
	public void onFail(Throwable t) {
		actions.onFail(t);
		observer.onFail(t);
	}
}
