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
package uk.co.strangeskies.mathematics.graph.processing;

import java.util.List;
import java.util.function.BiFunction;

import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.processing.GraphWalker.Marker;

public interface Pathfinder {
	public interface Solver<V, E> {
		List<V> verticesBetween(V from, V to);

		List<E> edgesBetween(V from, V to);
	}

	<V, E> Solver<V, E> over(Graph<V, E> graph);

	<V, E> Marker<V, E> over(Graph<V, E> graph,
			BiFunction<? super V, ? super V, ? extends Double> heuristic);
}