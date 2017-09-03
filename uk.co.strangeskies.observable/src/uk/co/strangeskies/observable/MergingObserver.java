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

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MergingObserver<T, U> extends PassthroughObserver<T, U> {
  private final Function<? super T, ? extends Observable<? extends U>> mapping;
  private final List<Observation> observations;
  private boolean cancelled;

  public MergingObserver(
      Observer<? super U> downstreamObserver,
      Function<? super T, ? extends Observable<? extends U>> mapping) {
    super(downstreamObserver);
    this.mapping = mapping;
    this.observations = new ArrayList<>();
  }

  @Override
  public void onObserve(Observation observation) {
    super.onObserve(new Observation() {
      @Override
      public void cancel() {
        observation.cancel();
        cancelled = true;
        synchronized (observations) {
          observations.forEach(Observation::cancel);
        }
      }

      @Override
      public void request(long count) {
        synchronized (observations) {
          if (count == Long.MAX_VALUE) {
            observations.forEach(Observation::requestUnbounded);
          } else {
            observations.sort(comparing(Observation::getPendingRequestCount));
            // TODO balance request between upstream observations
          }
        }
      }

      @Override
      public long getPendingRequestCount() {
        synchronized (observations) {
          return observations.stream().mapToLong(Observation::getPendingRequestCount).sum();
        }
      }
    });
    observation.requestUnbounded();
  }

  @Override
  public void onNext(T message) {
    synchronized (observations) {
      if (!cancelled) {
        mapping
            .apply(message)
            .then(Observer.onObservation(observations::add))
            .then(Observer.onFailure(this::onFail))
            .then(Observer.onCompletion(this::onComplete))
            .observe(getDownstreamObserver()::onNext);
      }
    }
  }
}