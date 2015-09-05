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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.utilities.Copyable;

/**
 * @author Elias N Vasylenko
 * @param <V>
 * @param <E>
 */
public interface Graph<V, E> extends Copyable<Graph<V, E>> {
	public interface Vertices<V> extends Set<V> {
		Set<V> adjacentTo(V vertex);

		Set<V> outgoingFrom(V vertex);

		Set<V> incomingTo(V vertex);

		Comparator<? super V> comparator();
	}

	public interface Edges<V, E> extends Map<E, EdgeVertices<V>> {
		Set<E> adjacentTo(V vertex);

		Set<E> outgoingFrom(V vertex);

		Set<E> incomingTo(V vertex);

		default Set<E> between(V from, V to) {
			return between(EdgeVertices.between(from, to));
		}

		Set<E> between(EdgeVertices<V> vertices);

		/**
		 * If there is exactly one edge between the provided vertices, this edge is
		 * returned. If there is no edge between the provided vertices, null is
		 * returned. If there are multiple edges between them, an exception is
		 * thrown.
		 *
		 * @param from
		 * @param to
		 * @return
		 */
		default E betweenUnique(V from, V to) {
			return betweenUnique(EdgeVertices.between(from, to));
		}

		E betweenUnique(EdgeVertices<V> vertices);

		default E add(V from, V to) {
			return add(EdgeVertices.between(from, to));
		}

		E add(EdgeVertices<V> edge);

		default boolean add(@SuppressWarnings("unchecked") EdgeVertices<V>... edges) {
			return add(Arrays.asList(edges));
		}

		default boolean add(Collection<? extends EdgeVertices<V>> edgeVertices) {
			boolean changed = false;
			for (EdgeVertices<V> edge : edgeVertices)
				changed = add(edge) != null | changed;
			return changed;
		}

		double weight(E edge);

		Comparator<? super E> comparator();
	}

	/**
	 * Returns an unmodifiable set of the vertices in this graph.
	 *
	 * @return
	 */
	Vertices<V> vertices();

	/**
	 * Returns an unmodifiable set of the edges in this graph.
	 *
	 * @return
	 */
	Edges<V, E> edges();

	boolean isDirected();

	boolean isWeighted();

	boolean isSimple();

	GraphTransformer<V, E> transform();
}
