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

import static java.lang.Thread.currentThread;

import java.util.Objects;

public class SafeObserver<T> extends PassthroughObserver<T, T> {
  private boolean done;
  private long pendingRequestCount;

  public SafeObserver(Observer<? super T> downstreamObserver) {
    super(downstreamObserver);
  }

  @Override
  public void onObserve(Observation observation) {
    Objects.requireNonNull(observation, "Observation must not be null");

    if (!assertCanMakeObservation(observation))
      return;

    pendingRequestCount = observation.getPendingRequestCount();

    Observation downstreamObservation = new Observation() {
      @Override
      public void request(long count) {
        if (isDone())
          return;

        if (pendingRequestCount + count < pendingRequestCount) {
          pendingRequestCount = Long.MAX_VALUE;
          observation.request(count);
        } else if (pendingRequestCount < Long.MAX_VALUE) {
          pendingRequestCount += count;
          observation.request(count);
        } else {
          pendingRequestCount = Long.MAX_VALUE;
        }
      }

      @Override
      public long getPendingRequestCount() {
        return pendingRequestCount;
      }

      @Override
      public void cancel() {
        SafeObserver.this.cancel();
        observation.cancel();
      }
    };

    tryAction(() -> {
      initializeObservation(downstreamObservation);
      getDownstreamObserver().onObserve(downstreamObservation);
    });
  }

  boolean isDone() {
    return done;
  }

  void cancel() {
    done = true;
  }

  protected boolean assertMadeObservation() {
    if (isObservationInitialized()) {
      return true;
    } else {
      unmanagedError(new IllegalStateException("Observation unavailable " + this));
      return false;
    }
  }

  protected boolean assertCanMakeObservation(Observation observation) {
    if (!isObservationInitialized()) {
      return true;
    } else {
      observation.cancel();
      unmanagedError(
          new IllegalStateException("Cannot make multiple concurrent observations " + this));
      return false;
    }
  }

  protected void unmanagedError(Throwable t) {
    currentThread().getUncaughtExceptionHandler().uncaughtException(currentThread(), t);
  }

  @Override
  protected void initializeObservation(Observation observation) {
    if (isObservationInitialized() && !isDone()) {
      observation.cancel();
    } else {
      super.initializeObservation(observation);
    }
  }

  @Override
  public void onNext(T message) {
    Objects.requireNonNull(message, "Observable message must not be null");

    if (!assertMadeObservation())
      return;

    if (pendingRequestCount == 0) {
      onFail(new UnexpectedMessageException("Unrequested message " + message));
      return;
    }

    tryAction(() -> {
      getDownstreamObserver().onNext(message);

      if (pendingRequestCount < Long.MAX_VALUE)
        pendingRequestCount--;
    });
  }

  @Override
  public void onComplete() {
    if (!assertMadeObservation())
      return;

    tryAction(() -> getDownstreamObserver().onComplete());
    cancel();
  }

  @Override
  public void onFail(Throwable t) {
    Objects.requireNonNull(t, "Observable failure throwable must not be null");

    if (!assertMadeObservation())
      return;

    tryAction(() -> {
      getDownstreamObserver().onFail(t);
    });
    cancel();
  }

  public void tryAction(Runnable action) {
    if (isDone()) {
      return;
    }

    try {
      action.run();
    } catch (VirtualMachineError | ThreadDeath | LinkageError t) {
      cancel();
      throw t;
    } catch (Throwable t) {
      cancel();
      try {
        getDownstreamObserver().onFail(t);
      } catch (Throwable u) {
        t.addSuppressed(u);
        unmanagedError(t);
      }
    }
  }
}
