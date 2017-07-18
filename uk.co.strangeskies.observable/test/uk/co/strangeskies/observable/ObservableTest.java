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

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class ObservableTest {
  @Test(timeout = 5000)
  public void weakReferenceTest() {
    WeakReference<?> reference = new WeakReference<>(new Object());

    while (reference.get() != null) {
      new Object();
      System.gc();
      System.runFinalization();
    }

    assertNull(reference.get());
  }

  @Test(timeout = 5000)
  public void holdWeakObserverTest() {
    HotObservable<String> stringObservable = new HotObservable<>();

    List<String> list = new ArrayList<>();

    stringObservable.weakReference(list).observe(m -> m.owner().add(m.message()));

    weakReferenceTest();

    stringObservable.sendNext("test");

    assertThat(list, contains("test"));
  }

  @Test(timeout = 5000)
  public void dropWeakObserverTest() {
    HotObservable<String> stringObservable = new HotObservable<>();

    List<String> list = new ArrayList<>();
    Observer<String> add = list::add;

    stringObservable.weakReference().observe(add);

    stringObservable.sendNext("test1");

    add = null;
    weakReferenceTest();

    stringObservable.sendNext("test2");

    assertThat(list, contains("test1"));
  }

  @Test
  public void observeEventTest() {
    List<String> list = new ArrayList<>();

    HotObservable<String> stringObservable = new HotObservable<>();
    stringObservable.observe(list::add);

    stringObservable.sendNext("test");

    assertThat(list, contains("test"));
  }

  @Test
  public void observeMultipleEventsTest() {
    List<String> list = new ArrayList<>();

    HotObservable<String> stringObservable = new HotObservable<>();
    stringObservable.observe(list::add);

    stringObservable.sendNext("test1");
    stringObservable.sendNext("test2");

    assertThat(list, contains("test1", "test2"));
  }
}
