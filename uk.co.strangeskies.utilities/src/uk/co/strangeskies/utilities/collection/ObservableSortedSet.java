/*
* Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.ObservableSortedSetDecorator.ObservableSortedSetDecoratorImpl;
import uk.co.strangeskies.utilities.collection.SynchronizedObservableSortedSet.SynchronizedObservableSortedSetImpl;
import uk.co.strangeskies.utilities.collection.UnmodifiableObservableSortedSet.UnmodifiableObservableSortedSetImpl;

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
public interface ObservableSortedSet<S extends ObservableSortedSet<S, E>, E> extends SortedSet<E>, ObservableSet<S, E> {
	@Override
	default ObservableSet<?, E> unmodifiableView() {
		return new UnmodifiableObservableSortedSetImpl<>(this);
	}

	/**
	 * As {@link #unmodifiableView()}, but a little more lenient with target type,
	 * taking advantage of the variance properties of a read-only collection.
	 * 
	 * @param <E>
	 *          the target element type
	 * @param list
	 *          the list over which we want a view
	 * @return an unmodifiable view over the given list
	 */
	static <E> ObservableSet<?, E> unmodifiableViewOf(ObservableSortedSet<?, ? extends E> set) {
		return new UnmodifiableObservableSortedSetImpl<>(set);
	}

	@Override
	default ObservableSet<?, E> synchronizedView() {
		return new SynchronizedObservableSortedSetImpl<>(this);
	}

	public static <C extends SortedSet<E>, E> ObservableSortedSet<?, E> over(C set,
			Function<? super C, ? extends C> copy) {
		return new ObservableSortedSetDecoratorImpl<C, E>(set, copy);
	}

	public static <E> ObservableSortedSet<?, E> ofElements(Collection<? extends E> elements) {
		ObservableSortedSet<?, E> set = new ObservableSortedSetDecoratorImpl<>(new TreeSet<>(), s -> new TreeSet<>(s));
		set.addAll(elements);
		return set;
	}

	@SafeVarargs
	public static <E> ObservableSet<?, E> ofElements(E... elements) {
		return ofElements(Arrays.asList(elements));
	}
}
