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

import static uk.co.strangeskies.observable.Observable.Observation.CONTINUE;
import static uk.co.strangeskies.observable.Observable.Observation.TERMINATE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.collection.observable.ObservableSetDecorator.ObservableSetDecoratorImpl;
import uk.co.strangeskies.collection.observable.SynchronizedObservableSet.SynchronizedObservableSetImpl;
import uk.co.strangeskies.collection.observable.UnmodifiableObservableSet.UnmodifiableObservableSetImpl;
import uk.co.strangeskies.utility.Copyable;
import uk.co.strangeskies.utility.IdentityProperty;
import uk.co.strangeskies.utility.Self;

/**
 * A set which can be observed for changes, as per the contract of
 * {@link ObservableCollection}.
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound, as per {@link Self}
 * @param <E>
 *          the element type, as per {@link Collection}
 */
public interface ObservableSet<S extends ObservableSet<S, E>, E>
		extends Set<E>, ObservableCollection<S, E, ObservableSet.Change<E>> {
	/**
	 * A change event for {@link ObservableSet}. All elements {@link #added()} or
	 * {@link #removed()} during the operation of a single change may be
	 * inspected. These events are aggregated without any ordering, and with
	 * multiple overlapping events, or events which cancel each other out, being
	 * ignored.
	 *
	 * @author Elias N Vasylenko
	 * @param <E>
	 *          the element type
	 */
	interface Change<E> {
		Set<E> added();

		Set<E> removed();
	}

	@Override
	default ObservableSet<?, E> unmodifiableView() {
		return new UnmodifiableObservableSetImpl<>(this);
	}

	/**
	 * As {@link #unmodifiableView()}, but a little more lenient with target type,
	 * taking advantage of the variance properties of a read-only collection.
	 * 
	 * @param <E>
	 *          the target element type
	 * @param set
	 *          the list over which we want a view
	 * @return an unmodifiable view over the given list
	 */
	static <E> ObservableSet<?, E> unmodifiableViewOf(ObservableSet<?, ? extends E> set) {
		return new UnmodifiableObservableSetImpl<>(set);
	}

	@Override
	default ObservableSet<?, E> synchronizedView() {
		return new SynchronizedObservableSetImpl<>(this);
	}

	public static <C extends Set<E>, E> ObservableSet<?, E> over(C set, Function<? super C, ? extends C> copy) {
		return new ObservableSetDecoratorImpl<>(set, copy);
	}

	public static <C extends Copyable<? extends C> & Set<E>, E> ObservableSet<?, E> over(C set) {
		return new ObservableSetDecoratorImpl<>(set, Copyable::copy);
	}

	public static <C extends Set<E>, E> ObservableSet<?, E> over(C set) {
		return over(set, s -> {
			throw new UnsupportedOperationException();
		});
	}

	public static <E> ObservableSet<?, E> ofElements(Collection<? extends E> elements) {
		ObservableSet<?, E> set = new ObservableSetDecoratorImpl<>(new HashSet<>(), s -> new HashSet<>(s));
		set.addAll(elements);
		return set;
	}

	@SafeVarargs
	public static <E> ObservableSet<?, E> ofElements(E... elements) {
		return ofElements(Arrays.asList(elements));
	}

	@Override
	Set<E> silent();

	default void waitFor(E element) throws InterruptedException {
		waitFor(element, () -> {});
	}

	default void waitFor(E element, Runnable onPresent) throws InterruptedException {
		waitFor(element, onPresent, -1);
	}

	default void waitFor(E element, int timeoutMilliseconds) throws InterruptedException {
		waitFor(element, () -> {}, timeoutMilliseconds);
	}

	default void waitFor(E element, Runnable onPresent, int timeoutMilliseconds) throws InterruptedException {
		IdentityProperty<Boolean> complete = new IdentityProperty<>(false);

		synchronized (complete) {
			changes().addTerminatingObserver(c -> {
				synchronized (complete) {
					if (complete.get()) {
						return CONTINUE;
					}

					if (c.added().contains(element)) {
						onPresent.run();

						complete.set(true);
						complete.notifyAll();

						return CONTINUE;
					}
				}

				return TERMINATE;
			});

			if (contains(element)) {
				complete.set(true);
				onPresent.run();
			} else {
				try {
					if (timeoutMilliseconds < 0) {
						complete.wait();
					} else {
						complete.wait(timeoutMilliseconds);
					}
				} catch (InterruptedException e) {
					synchronized (complete) {
						if (!complete.get()) {
							complete.set(true);
							throw e;
						}
					}
				}
			}
		}
	}
}
