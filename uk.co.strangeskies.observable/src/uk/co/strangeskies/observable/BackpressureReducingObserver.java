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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BackpressureReducingObserver<T, M> extends PassthroughObserver<T, M> {
  private final Supplier<? extends M> identity;
  private final Function<? super T, ? extends M> initial;
  private final BiFunction<? super M, ? super T, ? extends M> accumulator;

  private M current;
  private boolean complete;
  private final RequestCount outstandingRequests = new RequestCount();

  public BackpressureReducingObserver(
      Observer<? super M> downstreamObserver,
      Supplier<? extends M> identity,
      BiFunction<? super M, ? super T, ? extends M> accumulator) {
    super(downstreamObserver);
    this.identity = identity;
    this.initial = m -> accumulator.apply(identity.get(), m);
    this.accumulator = accumulator;
  }

  public BackpressureReducingObserver(
      Observer<? super M> downstreamObserver,
      Function<? super T, ? extends M> initial,
      BiFunction<? super M, ? super T, ? extends M> accumulator) {
    super(downstreamObserver);
    this.identity = null;
    this.initial = initial;
    this.accumulator = accumulator;
  }

  @Override
  public void onObserve(Observation observation) {
    observation.requestUnbounded();

    super.onObserve(new Observation() {
      @Override
      public void request(long count) {
        for (int i = 0; i < count; i++) {
          synchronized (outstandingRequests) {
            if (current == null) {
              if (complete) {
                getDownstreamObserver().onComplete();
                break;
              } else if (identity == null) {
                outstandingRequests.request(count);
                break;
              } else {
                current = identity.get();
              }
            }
            getDownstreamObserver().onNext(current);
            current = null;
          }
        }
      }

      @Override
      public void requestUnbounded() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void cancel() {
        observation.cancel();
      }
    });
  }

  @Override
  public void onNext(T message) {
    synchronized (outstandingRequests) {
      if (current == null)
        current = initial.apply(message);
      else
        current = accumulator.apply(current, message);

      if (!outstandingRequests.isFulfilled()) {
        getDownstreamObserver().onNext(current);
        current = null;
      }
    }
  }

  @Override
  public void onComplete() {
    synchronized (outstandingRequests) {
      complete = true;
    }
  }

  @Override
  public void onFail(Throwable t) {
    super.onFail(t);
  }
}
