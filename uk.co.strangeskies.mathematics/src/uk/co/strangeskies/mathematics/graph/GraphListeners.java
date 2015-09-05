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

import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface GraphListeners<V, E> {
	@ProviderType
	interface ChangeSet<V, E> {
		Set<V> verticesAdded();

		Set<V> verticesRemoved();

		Set<E> edgesAdded();

		Set<E> edgesRemoved();

		Set<E> edgesMoved();
	}

	@ConsumerType
	@FunctionalInterface
	interface ChangeListener<V, E> {
		void change(Graph<V, E> graph, ChangeSet<V, E> changeSet);
	}

	@ConsumerType
	@FunctionalInterface
	interface EdgeListener<V, E> {
		void edge(Graph<V, E> graph, E edge);
	}

	@ConsumerType
	@FunctionalInterface
	interface EdgesListener<V, E> {
		void edges(Graph<V, E> graph, Set<E> edges);
	}

	@ConsumerType
	@FunctionalInterface
	interface VertexListener<V, E> {
		void vertex(Graph<V, E> graph, V vertex);
	}

	@ConsumerType
	@FunctionalInterface
	interface VerticesListener<V, E> {
		void vertices(Graph<V, E> graph, Set<V> vertex);
	}

	Set<ChangeListener<V, E>> change();

	Set<EdgeListener<V, E>> edgeAdded();

	Set<EdgesListener<V, E>> edgesAdded();

	/**
	 * 
	 * <p>
	 * Internal listeners which are triggered during an atomic operation are
	 * considered a part of that operation. This means that any secondary
	 * mutations made to the graph by the listener will be discarded upon the
	 * failure of that operation.
	 * 
	 * @return
	 */
	Set<EdgeListener<V, E>> edgeRemoved();

	Set<EdgesListener<V, E>> edgesRemoved();

	/**
	 * 
	 * 
	 * <p>
	 * Internal listeners which are triggered during an atomic operation are
	 * considered a part of that operation. This means that any secondary
	 * mutations made to the graph by the listener will be discarded upon the
	 * failure of that operation.
	 * 
	 * @return
	 */
	Set<VertexListener<V, E>> vertexAdded();

	Set<VerticesListener<V, E>> verticesAdded();

	/**
	 * 
	 * 
	 * <p>
	 * Internal listeners which are triggered during an atomic operation are
	 * considered a part of that operation. This means that any secondary
	 * mutations made to the graph by the listener will be discarded upon the
	 * failure of that operation.
	 * 
	 * @return
	 */
	Set<VertexListener<V, E>> vertexRemoved();

	Set<VerticesListener<V, E>> verticesRemoved();
}
