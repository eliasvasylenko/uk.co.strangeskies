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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.utilities.factory.Factory;

public class ListTransformationFunction<F, T> extends
		CollectionTransformationFunction<F, T, List<T>> {

	public ListTransformationFunction(Function<? super F, ? extends T> function) {
		super(function, new Factory<List<T>>() {
			@Override
			public List<T> create() {
				return new ArrayList<>();
			}
		});
	}

	public static <X, Y> List<Y> apply(Collection<? extends X> collection,
			Function<? super X, Y> function) {
		return new ListTransformationFunction<X, Y>(function).apply(collection);
	}

	@SuppressWarnings("unchecked")
	public static <X, Y> Y[] apply(X[] collection,
			Function<? super X, ? extends Y> function) {
		return (Y[]) new ListTransformationFunction<X, Y>(function).apply(
				Arrays.asList(collection)).toArray();
	}
}