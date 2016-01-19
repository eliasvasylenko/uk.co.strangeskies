/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public class GraphListeners<V, E> {
	@ProviderType
	public interface ChangeSet<V, E> {
		Set<V> verticesAdded();

		Set<V> verticesRemoved();

		Map<E, EdgeVertices<V>> edgesAdded();

		Map<E, EdgeVertices<V>> edgesRemoved();
	}

	@ConsumerType
	@FunctionalInterface
	public interface ChangeListener<V, E> {
		void change(Graph<V, E> graph, ChangeSet<V, E> changeSet);
	}

	@ConsumerType
	@FunctionalInterface
	public interface EdgeListener<V, E> {
		void edge(Graph<V, E> graph, E edge, EdgeVertices<V> vertices);
	}

	@ConsumerType
	@FunctionalInterface
	public interface EdgesListener<V, E> {
		void edges(Graph<V, E> graph, Map<E, EdgeVertices<V>> edges);
	}

	@ConsumerType
	@FunctionalInterface
	public interface VertexListener<V, E> {
		void vertex(Graph<V, E> graph, V vertex);
	}

	@ConsumerType
	@FunctionalInterface
	public interface VerticesListener<V, E> {
		void vertices(Graph<V, E> graph, Set<V> vertex);
	}

	private final Set<ChangeListener<V, E>> change = new HashSet<>();
	private final Set<EdgeListener<V, E>> edgeAdded = new HashSet<>();
	private final Set<EdgesListener<V, E>> edgesAdded = new HashSet<>();
	private final Set<EdgeListener<V, E>> edgeRemoved = new HashSet<>();
	private final Set<EdgesListener<V, E>> edgesRemoved = new HashSet<>();
	private final Set<VertexListener<V, E>> vertexAdded = new HashSet<>();
	private final Set<VerticesListener<V, E>> verticesAdded = new HashSet<>();
	private final Set<VertexListener<V, E>> vertexRemoved = new HashSet<>();
	private final Set<VerticesListener<V, E>> verticesRemoved = new HashSet<>();

	public Set<ChangeListener<V, E>> change() {
		return change;
	}

	public Set<EdgeListener<V, E>> edgeAdded() {
		return edgeAdded;
	}

	public Set<EdgesListener<V, E>> edgesAdded() {
		return edgesAdded;
	}

	public Set<EdgeListener<V, E>> edgeRemoved() {
		return edgeRemoved;
	}

	public Set<EdgesListener<V, E>> edgesRemoved() {
		return edgesRemoved;
	}

	public Set<VertexListener<V, E>> vertexAdded() {
		return vertexAdded;
	}

	public Set<VerticesListener<V, E>> verticesAdded() {
		return verticesAdded;
	}

	public Set<VertexListener<V, E>> vertexRemoved() {
		return vertexRemoved;
	}

	public Set<VerticesListener<V, E>> verticesRemoved() {
		return verticesRemoved;
	}
}
