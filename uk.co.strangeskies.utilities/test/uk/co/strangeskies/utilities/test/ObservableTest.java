package uk.co.strangeskies.utilities.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Test;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.ObservableImpl;

public class ObservableTest {
	@Test
	public void immutableObservableTest() {
		Observable<String> observable = Observable.immutable();

		Consumer<String> listObserver = null;

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
		stringObservable.addTerminatingObserver(list, list::add);

		stringObservable.fire("test1");
		stringObservable.fire("test2");

		stringObservable.removeObserver(list);

		stringObservable.fire("test3");

		assertEquals(Arrays.asList("test1", "test2"), list);
	}

	@Test
	public void selfTerminatingObserverTest() {
		Set<String> list = new LinkedHashSet<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addTerminatingObserver(list::add);

		stringObservable.fire("test1");
		stringObservable.fire("test2");
		stringObservable.fire("test3");
		stringObservable.fire("test3");
		stringObservable.fire("test4");

		assertEquals(Arrays.asList("test1", "test2", "test3"), new ArrayList<>(list));
	}

	@Test
	public void observeEventTest() {
		List<String> list = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addObserver(list::add);

		stringObservable.fire("test");

		assertEquals(Arrays.asList("test"), list);
	}

	@Test
	public void observeMultipleEventsTest() {
		List<String> list = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addObserver(list::add);

		stringObservable.fire("test1");
		stringObservable.fire("test2");

		assertEquals(Arrays.asList("test1", "test2"), list);
	}

	@Test
	public void addRemoveTest() {
		List<String> list = new ArrayList<>();
		Consumer<String> listObserver = list::add;

		ObservableImpl<String> stringObservable = new ObservableImpl<>();

		assertFalse(stringObservable.removeObserver(listObserver));

		assertTrue(stringObservable.addObserver(listObserver));
		assertFalse(stringObservable.addObserver(listObserver));

		stringObservable.fire("test");

		assertTrue(stringObservable.removeObserver(listObserver));
		assertFalse(stringObservable.removeObserver(listObserver));

		assertThat(list, is(equalTo(asList("test"))));
	}

	@Test
	public void addAndRemovemultipleObserversTest() {
		List<String> list0 = new ArrayList<>();
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();

		ObservableImpl<String> stringObservable = new ObservableImpl<>();
		stringObservable.addObserver(list0, list0::add);

		stringObservable.addObserver(list1, list1::add);
		stringObservable.fire("test1");

		stringObservable.removeObserver(list1);
		stringObservable.addObserver(list2, list2::add);
		stringObservable.fire("test2");

		stringObservable.removeObserver(list2);
		stringObservable.addObserver(list3, list3::add);
		stringObservable.fire("test3");

		assertThat(list0, is(equalTo(asList("test1", "test2", "test3"))));
		assertThat(list1, is(equalTo(asList("test1"))));
		assertThat(list2, is(equalTo(asList("test2"))));
		assertThat(list3, is(equalTo(asList("test3"))));
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

		assertThat(stringObservable.getObservers(), is(equalTo(emptySet())));

		assertThat(list1, is(equalTo(asList("test1", "test2"))));
		assertThat(list2, is(equalTo(asList("test2"))));
		assertThat(list3, is(equalTo(asList())));
	}
}
