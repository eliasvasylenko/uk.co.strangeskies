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
package uk.co.strangeskies.mathematics.graph.processing;

import java.util.Set;

import uk.co.strangeskies.mathematics.graph.Graph;

public interface GraphWalker {
	public interface Marker<V, E> {
		V currentLocation();

		default boolean isTerminated() {
			return availableVertices().isEmpty();
		}

		Set<V> availableVertices();

		Set<E> availableEdges();

		E moveTo(V nextVertex);

		V moveThrough(E nextEdge);
	}

	<V, E> Marker<V, E> atEntryPoint(Graph<V, E> graph, V entryPoint);

	<V, E> Marker<V, E> atRoot(Graph<V, E> graph);
}
