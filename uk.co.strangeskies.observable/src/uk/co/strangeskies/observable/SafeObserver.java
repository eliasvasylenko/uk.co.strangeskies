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

import java.util.Objects;
import java.util.function.Consumer;

public class SafeObserver<T> extends PassthroughObserver<T, T> {
  private boolean disposed;

  public SafeObserver(Observer<? super T> downstreamObserver) {
    super(downstreamObserver);
  }

  public void cancel() {
    disposed = true;
  }

  public boolean isDisposed() {
    return disposed;
  }

  @Override
  public void onObserve(Observation upstreamObservation) {
    Observation downstreamObservation = upstreamObservation; // TODO

    tryAction(o -> o.onObserve(downstreamObservation));
  }

  @Override
  public void onNext(T message) {
    tryAction(o -> {
      Objects.requireNonNull(message, "Observable message must not be null");
      o.onNext(message);
    });
  }

  @Override
  public void onComplete() {
    tryAction(o -> o.onComplete());
  }

  @Override
  public void onFail(Throwable t) {
    tryAction(o -> {
      Objects.requireNonNull(t, "Observable failure throwable must not be null");
      o.onFail(t);
    });
  }

  public void tryAction(Consumer<? super Observer<? super T>> action) {
    if (disposed)
      return;

    try {
      action.accept(getDownstreamObserver());
    } catch (VirtualMachineError | ThreadDeath | LinkageError t) {
      throw t;
    } catch (Throwable t) {
      try {
        cancel();
        getDownstreamObserver().onFail(t);
      } catch (Throwable u) {

      }
    }
  }
}
