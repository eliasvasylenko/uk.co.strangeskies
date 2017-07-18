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
 * This file is part of uk.co.strangeskies.collections.observable.
 *
 * uk.co.strangeskies.collections.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.observable;

import java.util.Collections;
import java.util.Set;

import uk.co.strangeskies.collection.SetDecorator;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.Observation;
import uk.co.strangeskies.observable.Observer;

public abstract class UnmodifiableObservableSet<S extends ObservableSet<S, E>, E>
    implements SetDecorator<E>, ObservableSet<S, E> {
  static class UnmodifiableObservableSetImpl<E>
      extends UnmodifiableObservableSet<UnmodifiableObservableSetImpl<E>, E> {
    UnmodifiableObservableSetImpl(ObservableSet<?, ? extends E> component) {
      super(component);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnmodifiableObservableSetImpl<E> copy() {
      return new UnmodifiableObservableSetImpl<>(((ObservableSet<?, E>) getComponent()).copy());
    }
  }

  private final Set<E> component;

  private final Observable<S> observable;
  private final Observable<Change<E>> changes;

  @SuppressWarnings("unchecked")
  protected UnmodifiableObservableSet(ObservableSet<?, ? extends E> component) {
    this.component = Collections.unmodifiableSet(component);

    observable = component.weakReference(this).map(m -> m.owner().getThis());
    changes = component.changes().weakReference(this).map(m -> (Change<E>) m.message());
  }

  @Override
  public Set<E> getComponent() {
    return component;
  }

  @Override
  public Observation<S> observe(Observer<? super S> observer) {
    return observable.observe(observer);
  }

  @Override
  public Observable<Change<E>> changes() {
    return changes;
  }

  @Override
  public Set<E> silent() {
    return component;
  }

  @Override
  public String toString() {
    return getComponent().toString();
  }

  @Override
  public int hashCode() {
    return getComponent().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getComponent().equals(obj);
  }
}
