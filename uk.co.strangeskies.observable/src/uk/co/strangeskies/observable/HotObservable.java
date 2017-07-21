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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A simple implementation of {@link Observable} which maintains a list of
 * listeners to receive events fired with {@link #next(Object)}.
 * <p>
 * Addition and removal of observers, as well as the firing of events, are
 * synchronized on the implementation object.
 * <p>
 * This implementation does not support backpressure, so listeners which need to
 * control demand must compose the observable with e.g. a buffering or dropping
 * operation.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The type of event message to produce
 */
public class HotObservable<M> implements Observable<M> {
	private boolean live = true;
	private Set<HotObservation<M>> observations;

	@Override
	public Observation observe(Observer<? super M> observer) {
		HotObservation<M> observation = new HotObservation<>(this, observer);

		if (observations == null)
			observations = new LinkedHashSet<>();

		observations.add(observation);

		if (live)
			observation.startObservation();

		return observation;
	}

	public boolean hasObservers() {
		return observations != null;
	}

	void stopObservation(Observation observer) {
		if (observations.remove(observer) && observations.isEmpty()) {
			observations = null;
		}
	}

	private void forObservers(Consumer<HotObservation<M>> action) {
		if (observations != null && action != null) {
			for (HotObservation<M> observation : new ArrayList<>(observations)) {
				action.accept(observation);
			}
		}
	}

	void assertLive() {
		if (!live)
			throw new IllegalStateException();
	}

	void assertDead() {
		if (live)
			throw new IllegalStateException();
	}

	public HotObservable<M> start() {
		assertDead();
		forObservers(o -> o.startObservation());
		return this;
	}

	/**
	 * Fire the given message to all observers.
	 * 
	 * @param item
	 *          the message event to send
	 */
	public HotObservable<M> next(M item) {
		assertLive();
		forObservers(o -> o.getObserver().onNext(item));
		return this;
	}

	public HotObservable<M> complete() {
		assertLive();
		forObservers(o -> o.getObserver().onComplete());
		live = false;
		observations = null;
		return this;
	}

	public HotObservable<M> fail(Throwable t) {
		assertLive();
		forObservers(o -> o.getObserver().onFail(t));
		live = false;
		observations = null;
		return this;
	}
}
