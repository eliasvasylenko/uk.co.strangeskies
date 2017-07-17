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

import java.util.Iterator;
import java.util.Optional;

/**
 * A simple implementation of {@link Observable} which implements backpressure
 * over the elements of an iterable.
 * <p>
 * The implementation is intentionally basic, with messages being pushed to the
 * observer on the same thread which makes the {@link Observation#request(long)
 * request} by default. An executor can be added downstream.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The type of event message to produce
 */
public class ColdObservable<M> implements Observable<M> {
  private final Iterable<? extends M> iterable;

  public ColdObservable(Iterable<? extends M> iterable) {
    this.iterable = iterable;
  }

  @Override
  public synchronized Observation<M> observe(Observer<? super M> observer) {
    Observation<M> observation = new ColdObservation<>(iterable, observer);
    observer.onObserve(observation);
    return observation;
  }

  static class ColdObservation<M> implements Observation<M> {
    private final Observer<? super M> observer;
    private final Iterator<? extends M> iterator;

    private boolean complete;

    ColdObservation(Iterable<? extends M> iterable, Observer<? super M> observer) {
      this.observer = observer;
      this.iterator = iterable.iterator();
      complete = false;
    }

    @Override
    public void request(long count) {
      if (count < 0) {
        observer.onFail(new IllegalArgumentException());
        dispose();
      }

      Optional<M> next = getNext();
      while (next != null && --count > 0) {
        observer.onNext(next.orElse(null));
        next = getNext();
      }
    }

    @Override
    public void requestUnbounded() {
      Optional<M> next = getNext();
      while (next != null) {
        observer.onNext(next.orElse(null));
        next = getNext();
      }
    }

    private synchronized Optional<M> getNext() {
      complete = complete || !iterator.hasNext();
      return complete ? null : Optional.ofNullable(iterator.next());
    }

    @Override
    public synchronized void dispose() {
      complete = true;
    }

    @Override
    public synchronized boolean isDisposed() {
      return complete;
    }
  }
}
