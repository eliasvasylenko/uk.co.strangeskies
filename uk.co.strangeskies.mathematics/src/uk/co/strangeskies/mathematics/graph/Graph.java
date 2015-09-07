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
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.utilities.Copyable;

/**
 * @author Elias N Vasylenko
 * @param <V>
 * @param <E>
 */
@ProviderType
public interface Graph<V, E> extends Copyable<Graph<V, E>> {
	@ProviderType
	public interface Vertices<V, E> extends Set<V> {
		BiPredicate<? super V, ? super V> equality();

		Set<V> adjacentTo(V vertex);

		EdgeVertices<V> incidentTo(E edge);

		V incidentToHead(E edge);

		V incidentToTail(E edge);

		Set<V> successorsOf(V vertex);

		Set<V> predecessorsOf(V vertex);
	}

	@ProviderType
	public interface Edges<V, E> extends Set<E> {
		BiPredicate<? super E, ? super E> equality();

		double weight(E edge);

		Set<E> incidentTo(V vertex);

		Set<E> incidentToHead(V vertex);

		Set<E> incidentToTail(V vertex);

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

		Set<EdgeVertices<V>> edgeVertices();
	}

	/**
	 * Returns an unmodifiable set of the vertices in this graph.
	 *
	 * @return
	 */
	Vertices<V, E> vertices();

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

	GraphListeners<V, E> listeners();

	/**
	 * 
	 * @param action
	 */
	void atomic(Consumer<? super Graph<V, E>> action);

	Set<V> createVertexSet();

	Set<V> createVertexSet(Collection<? extends V> vertices);

	Set<E> createEdgeSet();

	Set<E> createEdgeSet(Collection<? extends E> edges);

	<T> Map<V, T> createVertexMap();

	<T> Map<V, T> createVertexMap(Map<? extends V, ? extends T> edges);

	<T> Map<E, T> createEdgeMap();

	<T> Map<E, T> createEdgeMap(Map<? extends E, ? extends T> edges);
}
