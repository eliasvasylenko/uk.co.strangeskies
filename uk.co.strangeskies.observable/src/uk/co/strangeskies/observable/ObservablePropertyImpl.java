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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A simple implementation of {@link ObservableProperty} which maintains a list
 * of listeners to receive change events fired with {@link #set(Object)}.
 * <p>
 * Addition and removal of observers, as well as the firing of events, are
 * synchronized on the implementation object.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of event message to produce
 */
public class ObservablePropertyImpl<T> extends HotObservable<T> implements ObservableProperty<T> {
  enum State {
    VALUE, FAILURE, COMPLETE_FAILURE, COMPLETE_VALUE
  }

  class ChangeImpl implements Change<T> {
    private final T previous;
    private final T current;
    private final Throwable previousFailure;
    private final Throwable currentFailure;

    ChangeImpl(T previous, T current, Throwable previousFailure, Throwable currentFailure) {
      this.previous = previous;
      this.current = current;
      this.previousFailure = previousFailure;
      this.currentFailure = currentFailure;
    }

    @Override
    public T newValue() {
      if (currentFailure != null)
        throw new MissingValueException(ObservablePropertyImpl.this, currentFailure);
      return current;
    }

    @Override
    public T previousValue() {
      if (previousFailure != null)
        throw new MissingValueException(ObservablePropertyImpl.this, previousFailure);
      return previous;
    }

    @Override
    public Optional<T> tryNewValue() {
      if (currentChange == this) {
        synchronized (ObservablePropertyImpl.this) {
          if (currentChange == this) {
            currentChange = null;
          }
        }
      }
      return Optional.ofNullable(current);
    }

    @Override
    public Optional<T> tryPreviousValue() {
      return Optional.ofNullable(previous);
    }
  }

  private boolean complete;
  private T value;
  private Throwable failure;

  private final BiFunction<T, T, T> assignmentFunction;
  private final BiPredicate<T, T> equality;
  private ChangeImpl currentChange;

  public ObservablePropertyImpl(
      BiFunction<T, T, T> assignmentFunction,
      BiPredicate<T, T> equality,
      T initialValue) {
    this.assignmentFunction = assignmentFunction;
    this.equality = equality;
    value = initialValue;
  }

  @Override
  public Observable<Change<T>> changes() {
    return observable -> observe(new PassthroughObserver<T, Change<T>>(observable) {
      private T previous;
      private Throwable previousFailure;

      private Change<T> nextMessage() {
        return new ChangeImpl(previous, value, previousFailure, failure);
      }

      @Override
      public void onNext(T message) {
        observable.onNext(nextMessage());

        previous = message;
        previousFailure = null;
      }

      @Override
      public void onFail(Throwable t) {
        observable.onNext(nextMessage());

        previous = null;
        previousFailure = t;
      }
    });
  }

  @Override
  public HotObservable<T> start() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObservablePropertyImpl<T> next(T item) {
    if (complete)
      throw new IllegalStateException();

    item = assignmentFunction.apply(item, this.value);
    if (failure != null || !equality.test(this.value, item)) {
      failure = null;
      value = item;
      super.next(item);
    }
    return this;
  }

  @Override
  public ObservablePropertyImpl<T> fail(Throwable t) {
    if (complete)
      throw new IllegalStateException();

    value = null;

    if (t == null) {
      failure = new NullPointerException();
      super.fail(t);
      throw (NullPointerException) failure;
    }

    failure = t;
    super.fail(failure);
    return this;
  }

  @Override
  public ObservablePropertyImpl<T> complete() {
    complete = true;
    super.complete();
    return this;
  }

  @Override
  public Disposable observe(Observer<? super T> observer) {
    Disposable disposable = super.observe(observer);

    if (failure == null)
      observer.onNext(value);
    else
      observer.onFail(failure);

    if (complete)
      observer.onComplete();

    return disposable;
  }

  /**
   * Fire the given message to all observers.
   * 
   * @param value
   *          the message event to send
   */
  @Override
  public synchronized T set(T value) {
    T previous = this.value;
    next(value);
    return previous;
  }

  @Override
  public T get() {
    if (failure != null)
      throw new MissingValueException(this, failure);
    return value;
  }
}
