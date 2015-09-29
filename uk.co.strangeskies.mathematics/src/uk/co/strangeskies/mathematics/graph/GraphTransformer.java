/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.graph;

import java.util.Comparator;
import java.util.function.Function;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.utilities.factory.Factory;

@ProviderType
public interface GraphTransformer<V, E> extends Factory<Graph<V, E>> {
	public <W> GraphTransformer<W, E> vertices(Function<V, W> transformation);

	public <F> GraphTransformer<V, F> edges(Function<E, F> transformation);

	public GraphTransformer<V, E> edgeWeight(Function<E, Double> weight,
			boolean mutable);

	public GraphTransformer<V, E> direction(Comparator<V> lowToHigh);
}