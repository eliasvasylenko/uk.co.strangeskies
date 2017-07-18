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

import java.lang.ref.Reference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReferenceOwnedObservation<O, M> extends PassthroughObservation<M, OwnedMessage<O, M>> {
  private final Observer<? super OwnedMessage<O, M>> observer;
  private final Reference<O> ownerReference;

  public ReferenceOwnedObservation(
      Observable<? extends M> parentObservable,
      Observer<? super OwnedMessage<O, M>> observer,
      O owner,
      Function<O, Reference<O>> referenceFunction) {
    this.observer = observer;
    this.ownerReference = referenceFunction.apply(owner);

    passthroughObservation(parentObservable);
  }

  public void withOwner(Consumer<O> action) {
    O owner = ownerReference.get();
    if (owner != null) {
      action.accept(owner);
    } else {
      dispose();
    }
  }

  @Override
  public void onObserve() {
    observer.onObserve(this);
  }

  @Override
  public void onNext(M message) {
    withOwner(o -> observer.onNext(new OwnedMessage<O, M>() {
      @Override
      public O owner() {
        return o;
      }

      @Override
      public M message() {
        return message;
      }
    }));
  }

  @Override
  public void onComplete() {
    observer.onComplete();
  }

  @Override
  public void onFail(Throwable t) {
    observer.onFail(t);
  }
}
