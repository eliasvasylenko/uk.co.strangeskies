/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.function.collection;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.utilities.factory.Factory;

public class CollectionTransformationFunction<F, T, C extends Collection<T>>
		implements Function<Collection<? extends F>, C> {
	private final Function<? super F, ? extends T> function;
	private final Factory<C> collectionFactory;

	public CollectionTransformationFunction(
			Function<? super F, ? extends T> function, Factory<C> collectionFactory) {
		this.function = function;
		this.collectionFactory = collectionFactory;
	}

	@Override
	public final C apply(Collection<? extends F> input) {
		C collection = collectionFactory.create();
		for (F item : input) {
			collection.add(function.apply(item));
		}
		return collection;
	}

	public final Function<? super F, ? extends T> getFunction() {
		return function;
	}
}
