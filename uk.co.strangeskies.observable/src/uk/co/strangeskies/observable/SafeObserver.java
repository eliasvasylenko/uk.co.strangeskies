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

  public SafeObserver(Observer<? super T> downstreamObserver) {
    super(downstreamObserver);
  }

  @Override
  public void onObserve(Observation upstreamObservation) {
    Observation downstreamObservation = new Observation() {
      @Override
      public void request(long count) {
        // TODO Auto-generated method stub
        upstreamObservation.request(count);
      }

      @Override
      public void cancel() {
        upstreamObservation.cancel();
        done = true;
      }
    };

    tryAction(() -> {
      initializeObservation(downstreamObservation);
      getDownstreamObserver().onObserve(downstreamObservation);
    });
  }

  protected boolean assertMadeObservation() {
    try {
      getObservation();
      return true;
    } catch (Throwable t) {
      done = true;
      unmanagedError(t);
      return false;
    }
  }

  protected void unmanagedError(Throwable t) {
    currentThread().getUncaughtExceptionHandler().uncaughtException(currentThread(), t);
  }

  @Override
  public void onNext(T message) {
    if (!assertMadeObservation())
      return;

    tryAction(() -> {
      Objects.requireNonNull(message, "Observable message must not be null");
      getDownstreamObserver().onNext(message);
    });
  }

  @Override
  public void onComplete() {
    if (!assertMadeObservation())
      return;

    tryAction(() -> getDownstreamObserver().onComplete());
    done = true;
  }

  @Override
  public void onFail(Throwable t) {
    if (!assertMadeObservation())
      return;

    tryAction(() -> {
      Objects.requireNonNull(t, "Observable failure throwable must not be null");
      getDownstreamObserver().onFail(t);
    });
    done = true;
  }

  public void tryAction(Runnable action) {
    if (done) {
      return;
    }

    try {
      action.run();
    } catch (VirtualMachineError | ThreadDeath | LinkageError t) {
      done = true;
      throw t;
    } catch (Throwable t) {
      done = true;
      try {
        getDownstreamObserver().onFail(t);
      } catch (Throwable u) {
        t.addSuppressed(u);
        unmanagedError(t);
      }
    }
  }
}
