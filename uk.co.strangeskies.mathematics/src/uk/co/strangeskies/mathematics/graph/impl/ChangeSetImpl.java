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
package uk.co.strangeskies.mathematics.graph.impl;

import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.GraphListeners.ChangeListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.ChangeSet;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgeListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgesListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VertexListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VerticesListener;

class ChangeSetImpl<V, E> implements ChangeSet<V, E> {
	private final Graph<V, E> graph;

	private final Set<V> verticesAdded;
	private final Set<V> verticesRemoved;
	private final Map<E, EdgeVertices<V>> edgesAdded;
	private final Map<E, EdgeVertices<V>> edgesRemoved;

	public ChangeSetImpl(Graph<V, E> graph) {
		this.graph = graph;

		verticesAdded = graph.createVertexSet();
		verticesRemoved = graph.createVertexSet();
		edgesAdded = graph.createEdgeMap();
		edgesRemoved = graph.createEdgeMap();
	}

	private ChangeSetImpl(ChangeSetImpl<V, E> changeSet) {
		graph = changeSet.graph;

		verticesAdded = graph.createVertexSet(changeSet.verticesAdded);
		verticesRemoved = graph.createVertexSet(changeSet.verticesRemoved);
		edgesAdded = graph.createEdgeMap(changeSet.edgesAdded);
		edgesRemoved = graph.createEdgeMap(changeSet.edgesRemoved);
	}

	public ChangeSetImpl<V, E> copy() {
		return new ChangeSetImpl<>(this);
	}

	@Override
	public Set<V> verticesAdded() {
		return verticesAdded;
	}

	@Override
	public Set<V> verticesRemoved() {
		return verticesRemoved;
	}

	@Override
	public Map<E, EdgeVertices<V>> edgesAdded() {
		return edgesAdded;
	}

	@Override
	public Map<E, EdgeVertices<V>> edgesRemoved() {
		return edgesRemoved;
	}

	public void tryTriggerListeners(GraphListeners<V, E> listeners) {
		/*
		 * Vertices added and removed:
		 */
		for (V vertex : verticesAdded())
			for (VertexListener<V, E> listener : listeners.vertexAdded())
				listener.vertex(graph, vertex);

		for (V vertex : verticesRemoved())
			for (VertexListener<V, E> listener : listeners.vertexRemoved())
				listener.vertex(graph, vertex);

		for (VerticesListener<V, E> listener : listeners.verticesAdded())
			listener.vertices(graph, graph.createVertexSet(verticesAdded()));

		for (VerticesListener<V, E> listener : listeners.verticesRemoved())
			listener.vertices(graph, graph.createVertexSet(verticesRemoved()));

		/*
		 * Edges added and removed:
		 */
		for (E edge : edgesAdded().keySet())
			for (EdgeListener<V, E> listener : listeners.edgeAdded())
				listener.edge(graph, edge, edgesAdded().get(edge));

		for (E edge : edgesRemoved().keySet())
			for (EdgeListener<V, E> listener : listeners.edgeRemoved())
				listener.edge(graph, edge, edgesRemoved().get(edge));

		for (EdgesListener<V, E> listener : listeners.edgesAdded())
			listener.edges(graph, graph.createEdgeMap(edgesAdded()));

		for (EdgesListener<V, E> listener : listeners.edgesRemoved())
			listener.edges(graph, graph.createEdgeMap(edgesRemoved()));

		/*
		 * Change sets:
		 */
		for (ChangeListener<V, E> listener : listeners.change())
			listener.change(graph, this);
	}
}
