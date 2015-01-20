/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.function.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityComparator;

/**
 * A view of a list which will be automatically updated along with the original,
 * but who's elements will be a transformation of the original associated
 * elements by way of the function passed to the constructor. The implementation
 * employs lazy evaluation, so try to use get() as little as possible by reusing
 * the result.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          The type of the elements of this list.
 * @param <F>
 *          The type of the elements of the backing list.
 */
public class SetTransformOnceView<F, T> extends AbstractSet<T> {
	private final Collection<? extends F> backingCollection;
	private final Function<? super F, ? extends T> function;

	private final Map<F, T> transformations;

	public SetTransformOnceView(Collection<? extends F> backingCollection,
			final Function<? super F, ? extends T> function) {
		transformations = new TreeMap<>(new IdentityComparator<>());

		this.backingCollection = backingCollection;
		this.function = function;
	}

	public final Collection<? extends F> getBackingCollection() {
		return backingCollection;
	}

	public final Function<? super F, ? extends T> getFunction() {
		return function;
	}

	@Override
	public final int size() {
		return backingCollection.size();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends F> backingIterator = backingCollection.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return backingIterator.hasNext();
			}

			@Override
			public T next() {
				F backingElement = backingIterator.next();
				T transformation = transformations.get(backingElement);
				if (transformation == null) {
					transformation = function.apply(backingElement);
					transformations.put(backingElement, transformation);

					if (transformations.keySet().size() > backingCollection.size() * 1.5) {
						transformations.keySet().retainAll(backingCollection);
					}
				}
				return transformation;
			}

			@Override
			public void remove() {
				backingIterator.remove();
			}
		};
	}
}
