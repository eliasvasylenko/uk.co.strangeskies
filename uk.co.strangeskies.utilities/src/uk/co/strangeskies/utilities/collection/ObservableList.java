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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.utilities.collection.ObservableListDecorator.ObservableListDecoratorImpl;
import uk.co.strangeskies.utilities.collection.SynchronizedObservableList.SynchronizedObservableListImpl;
import uk.co.strangeskies.utilities.collection.UnmodifiableObservableList.UnmodifiableObservableListImpl;

public interface ObservableList<S extends ObservableList<S, E>, E>
		extends List<E>, ObservableCollection<S, E, ObservableList.Change<E>> {
	/**
	 * A change event for {@link ObservableList}. All elements added or removed
	 * during a change operation may be inspected, along with their indices before
	 * or after removal or addition respectively.
	 * 
	 * <p>
	 * The original order of sub-events before aggregation is lost. The effective
	 * ordering of the aggregated change operation is removal, then addition.
	 * 
	 * <p>
	 * Events
	 *
	 * @author Elias N Vasylenko
	 * @param <E>
	 */
	interface Change<E> {
		int[] removedIndices();

		List<E> removedItems();

		/*- TODO modification
		int[] modifiedIndices();
		
		List<E> modifiedFromItems();
		 */

		/*- TODO permutation
		int[] permutedFromIndices();
		
		int[] permutedToIndices();
		 */

		int[] addedIndices();

		List<E> addedItems();
	}

	@Override
	default ObservableList<?, E> unmodifiableView() {
		return new UnmodifiableObservableListImpl<>(this);
	}

	static <E> ObservableList<?, E> unmodifiableViewOf(ObservableList<?, ? extends E> list) {
		return new UnmodifiableObservableListImpl<>(list);
	}

	@Override
	default ObservableList<?, E> synchronizedView() {
		return new SynchronizedObservableListImpl<>(this);
	}

	public static <C extends List<E>, E> ObservableList<?, E> over(C list, Function<? super C, ? extends C> copy) {
		return new ObservableListDecoratorImpl<C, E>(list, copy);
	}

	public static <E> ObservableList<?, E> ofElements(Collection<? extends E> elements) {
		ObservableList<?, E> set = new ObservableListDecoratorImpl<>(new ArrayList<>(), s -> new ArrayList<>(s));
		set.addAll(elements);
		return set;
	}

	@SafeVarargs
	public static <E> ObservableList<?, E> ofElements(E... elements) {
		return ofElements(Arrays.asList(elements));
	}
}
