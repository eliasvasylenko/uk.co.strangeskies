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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.utilities.factory.Factory;

public class SetTransformationFunction<F, T> extends
		CollectionTransformationFunction<F, T, Set<T>> {

	public SetTransformationFunction(Function<? super F, ? extends T> function) {
		super(function, new Factory<Set<T>>() {
			@Override
			public Set<T> create() {
				return new HashSet<>();
			}
		});
	}

	public static <X, Y> Set<Y> apply(Collection<? extends X> collection,
			Function<? super X, ? extends Y> function) {
		return new SetTransformationFunction<X, Y>(function).apply(collection);
	}
}
