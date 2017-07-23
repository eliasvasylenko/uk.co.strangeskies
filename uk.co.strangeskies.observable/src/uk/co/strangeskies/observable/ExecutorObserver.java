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

import java.util.concurrent.Executor;

public class ExecutorObserver<T> extends PassthroughObserver<T, T> {
  private final Executor executor;
  private RequestCount requests;

  public ExecutorObserver(Observer<? super T> downstreamObserver, Executor executor) {
    super(downstreamObserver);
    this.executor = executor;
    this.requests = new RequestCount();
  }

  @Override
  public void onNext(T message) {
    executor.execute(() -> {
      getDownstreamObserver().onNext(message);
      boolean outstandingRequests;
      synchronized (requests) {
        requests.fulfil();
        outstandingRequests = requests.isFulfilled();
      }
      if (outstandingRequests) {
        getObservation().requestNext();
      }
    });
  }

  @Override
  public void onObserve(Observation observation) {
    Observation downstreamObservation = new Observation() {
      @Override
      public void request(long count) {
        requests.request(count);
        observation.requestNext();
      }

      @Override
      public void requestUnbounded() {
        requests.requestUnbounded();
      }

      @Override
      public void cancel() {
        observation.cancel();
      }
    };
    configureObservation(downstreamObservation);
    executor.execute(() -> super.onObserve(downstreamObservation));
  }

  @Override
  public void onComplete() {
    executor.execute(() -> super.onComplete());
  }

  @Override
  public void onFail(Throwable t) {
    executor.execute(() -> super.onFail(t));
  }
}
