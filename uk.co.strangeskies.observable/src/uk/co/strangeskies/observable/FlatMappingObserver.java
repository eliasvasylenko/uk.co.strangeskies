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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Function;

public class FlatMappingObserver<T, U> extends PassthroughObserver<T, U> {
  private final RequestAllocator requestAllocator;
  private final Function<? super T, ? extends Observable<? extends U>> mapping;

  private final LinkedHashMap<Observer<? extends U>, Observation> observations;
  private long outstandingRequests;
  private boolean cancelled;

  public FlatMappingObserver(
      Observer<? super U> downstreamObserver,
      Function<? super T, ? extends Observable<? extends U>> mapping,
      RequestAllocator requestAllocator) {
    super(downstreamObserver);
    this.mapping = requireNonNull(mapping);
    this.observations = new LinkedHashMap<>();
    this.requestAllocator = requireNonNull(requestAllocator);
  }

  private void allocateRequests() {
    synchronized (observations) {
      if (!observations.isEmpty()) {
        outstandingRequests = requestAllocator
            .allocateRequests(outstandingRequests, new ArrayList<>(observations.values()));

      } else if (getObservation().getPendingRequestCount() == 0) {
        getObservation().requestNext();
      }
    }
  }

  @Override
  public void onObserve(Observation observation) {
    super.onObserve(new Observation() {
      @Override
      public void cancel() {
        observation.cancel();
        cancelled = true;
        synchronized (observations) {
          observations.values().forEach(Observation::cancel);
        }
      }

      @Override
      public void request(long count) {
        synchronized (observations) {
          outstandingRequests += count;
          allocateRequests();
        }
      }

      @Override
      public long getPendingRequestCount() {
        synchronized (observations) {
          return observations
              .values()
              .stream()
              .mapToLong(Observation::getPendingRequestCount)
              .sum();
        }
      }
    });
    observation.requestUnbounded();
  }

  @Override
  public void onNext(T message) {
    synchronized (observations) {
      if (!cancelled) {
        mapping.apply(message).observe(new Observer<U>() {
          @Override
          public void onNext(U message) {
            getDownstreamObserver().onNext(message);
            synchronized (observations) {
              if (observations.get(this).getPendingRequestCount() == 0) {
                allocateRequests();
              }
            }
          }

          @Override
          public void onObserve(Observation observation) {
            synchronized (observations) {
              observations.put(this, observation);
              allocateRequests();
            }
          }

          @Override
          public void onComplete() {
            synchronized (observations) {
              getObservation().request(observations.remove(this).getPendingRequestCount());
            }
          }

          @Override
          public void onFail(Throwable t) {
            synchronized (observations) {
              observations.values().forEach(Observation::cancel);
            }
            getDownstreamObserver().onFail(t);
          }
        });
      }
    }
  }
}
