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

import java.lang.ref.Reference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReferenceObserver<M> extends PassthroughObserver<M, M> {
	public ReferenceObserver(
			Observer<? super M> downstreamObserver,
			Function<Observer<? super M>, Reference<Observer<? super M>>> referenceFunction) {
		super(referenceFunction.apply(downstreamObserver)::get);
	}

	public void withObserver(Consumer<Observer<? super M>> action) {
		Observer<? super M> observer = getDownstreamObserver();
		if (observer != null) {
			action.accept(observer);
		} else {
			getObservation().dispose();
		}
	}

	@Override
	public void onObserve(Observation observation) {
		configureObservation(observation);
		withObserver(o -> o.onObserve(observation));
	}

	@Override
	public void onNext(M message) {
		withObserver(o -> o.onNext(message));
	}

	@Override
	public void onComplete() {
		withObserver(o -> o.onComplete());
	}

	@Override
	public void onFail(Throwable t) {
		withObserver(o -> o.onFail(t));
	}
}
