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

/**
 * A value which can be {@link #get() fetched} and observed for updates and
 * {@link #changes() changes}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value
 */
public interface ObservableValue<T> extends Observable<T> {
  /**
   * A value change event.
   * 
   * @author Elias N Vasylenko
   *
   * @param <T>
   *          the type of the value
   */
  interface Change<T> {
    T newValue();

    T previousValue();
  }

  /**
   * @return the current value
   */
  T get();

  /**
   * @return an observable over changes to the value
   */
  Observable<Change<T>> changes();

  /**
   * @param <T>
   *          the type of the immutable value
   * @param value
   *          the immutable value to create an observable over
   * @return an observable over the given value which never changes or fires
   *         events
   */
  static <T> ObservableValue<T> immutableOver(T value) {
    return (ImmutableObservableValue<T>) () -> value;
  }
}
