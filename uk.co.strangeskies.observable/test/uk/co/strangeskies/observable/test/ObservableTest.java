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
package uk.co.strangeskies.observable.test;

import static java.util.Collections.emptySet;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.co.strangeskies.observable.Observable.Observation.CONTINUE;
import static uk.co.strangeskies.observable.Observable.Observation.TERMINATE;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableImpl;
import uk.co.strangeskies.observable.Observer;

@SuppressWarnings("javadoc")
public class ObservableTest {
	@Test
	public void ownedObserverEqualityTest() {
		class StringObservable extends Observable.OwnedObserverImpl<String> {
			protected StringObservable(Observer<? super String> observer, Object owner) {
				super(observer, owner);
			}
		}

		String first = new String("test");
		String second = new String("test");

		assertEquals(new StringObservable(null, first), new StringObservable(null, first));

		assertEquals(new StringObservable(null, second), new StringObservable(null, second));

		assertNotEquals(new StringObservable(null, first), new StringObservable(null, second));

		assertNotEquals(new StringObservable(null, first), new Object());

		Observer<String> Observer = new Observable.OwnedObserverImpl<String>(null, first) {};
		assertEquals(Observer, Observer);
	}

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
		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		List<String> list = new ArrayList<>();

		stringObservable.addWeakObserver(list, l -> s -> l.add(s));

		weakReferenceTest();

		stringObservable.fire("test");

		assertThat(list, contains("test"));
	}

	@Test(timeout = 5000)
	public void dropWeakObserverTest() {
		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		List<String> list = new ArrayList<>();

		stringObservable.addWeakObserver(list::add);

		stringObservable.fire("test1");

		weakReferenceTest();

		stringObservable.fire("test2");

		assertThat(list, contains("test1"));
	}

	@Test(timeout = 5000)
	public void onlyAllowSingleWeakObserverTest() {
		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		Observer<String> observer = new ArrayList<>()::add;

		assertTrue(stringObservable.addWeakObserver(observer));
		assertFalse(stringObservable.addWeakObserver(observer));
	}

	@Test
	public void removeOwnedObservableTest() {
		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		List<String> list = new ArrayList<>();
		Observer<String> listObserver = list::add;

		assertTrue(stringObservable.addOwnedObserver(listObserver, listObserver));
		assertFalse(stringObservable.removeObserver(list::remove));
		stringObservable.fire("test1");
		assertTrue(stringObservable.removeOwnedObserver(listObserver));
		stringObservable.fire("test2");

		assertThat(list, contains("test1"));
	}

	@Test
	public void immutableObservableTest() {
		Observable<String> observable = Observable.immutable();

		Observer<String> listObserver = null;

		assertTrue(observable.removeObserver(listObserver));

		assertTrue(observable.addObserver(listObserver));
		assertTrue(observable.addObserver(listObserver));

		assertTrue(observable.removeObserver(listObserver));
		assertTrue(observable.removeObserver(listObserver));
	}

	@Test
	public void manuallyRemoveSelfTerminatingObserverTest() {
		List<String> list = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addTerminatingObserver(list, e -> list.add(e) ? CONTINUE : TERMINATE);

		stringObservable.fire("test1");
		stringObservable.fire("test2");

		stringObservable.removeTerminatingObserver(list);

		stringObservable.fire("test3");

		assertThat(list, contains("test1", "test2"));
	}

	@Test
	public void selfTerminatingObserverTest() {
		Set<String> list = new LinkedHashSet<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addTerminatingObserver(e -> list.add(e) ? CONTINUE : TERMINATE);

		stringObservable.fire("test1");
		stringObservable.fire("test2");
		stringObservable.fire("test3");
		stringObservable.fire("test3");
		stringObservable.fire("test4");

		assertThat(list, contains("test1", "test2", "test3"));
	}

	@Test
	public void observeEventTest() {
		List<String> list = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addObserver(list::add);

		stringObservable.fire("test");

		assertThat(list, contains("test"));
	}

	@Test
	public void observeMultipleEventsTest() {
		List<String> list = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addObserver(list::add);

		stringObservable.fire("test1");
		stringObservable.fire("test2");

		assertThat(list, contains("test1", "test2"));
	}

	@Test
	public void addRemoveTest() {
		List<String> list = new ArrayList<>();
		Observer<String> listObserver = list::add;

		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		assertFalse(stringObservable.removeObserver(listObserver));

		assertTrue(stringObservable.addObserver(listObserver));
		assertFalse(stringObservable.addObserver(listObserver));

		stringObservable.fire("test");

		assertTrue(stringObservable.removeObserver(listObserver));
		assertFalse(stringObservable.removeObserver(listObserver));

		assertThat(list, contains("test"));
	}

	@Test
	public void addAndRemovemultipleObserversTest() {
		List<String> list0 = new ArrayList<>();
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addOwnedObserver(list0, list0::add);

		stringObservable.addOwnedObserver(list1, list1::add);
		stringObservable.fire("test1");

		stringObservable.removeOwnedObserver(list1);
		stringObservable.addOwnedObserver(list2, list2::add);
		stringObservable.fire("test2");

		stringObservable.removeOwnedObserver(list2);
		stringObservable.addOwnedObserver(list3, list3::add);
		stringObservable.fire("test3");

		assertThat(list0, contains("test1", "test2", "test3"));
		assertThat(list1, contains("test1"));
		assertThat(list2, contains("test2"));
		assertThat(list3, contains("test3"));
	}

	@Test
	public void clearObserversTest() {
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		stringObservable.addObserver(list1::add);
		stringObservable.fire("test1");

		stringObservable.addObserver(list2::add);
		stringObservable.fire("test2");

		stringObservable.addObserver(list3::add);
		stringObservable.clearObservers();
		stringObservable.fire("test3");

		stringObservable.clearObservers();
		stringObservable.fire("test4");

		stringObservable.addObserver(list3::add);
		stringObservable.fire("test5");

		stringObservable.clearObservers();

		assertThat(stringObservable.getObservers(), is(equalTo(emptySet())));

		assertThat(list1, contains("test1", "test2"));
		assertThat(list2, contains("test2"));
		assertThat(list3, contains("test5"));
	}
}
