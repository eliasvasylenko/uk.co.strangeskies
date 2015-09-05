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
package uk.co.strangeskies.mathematics.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphTransformer;
import uk.co.strangeskies.utilities.collection.decorator.MapDecorator;
import uk.co.strangeskies.utilities.collection.decorator.SetDecorator;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiTreeMap;

class GraphImpl<V, E> implements Graph<V, E> {
	private class VerticesImpl extends SetDecorator<V> implements Vertices<V> {
		public VerticesImpl() {
			super(adjacencyMatrix.keySet());
		}

		@Override
		public boolean add(V e) {
			return addVertex.test(e);
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			boolean changed = false;
			for (V v : c)
				changed = add(v) || changed;
			return changed;
		}

		@Override
		public boolean remove(Object vertex) {
			adjacencyMatrix.remove(vertex).values()
					.forEach(s -> s.forEach(e -> edges.remove(e)));

			adjacencyMatrix.values().forEach(toMap -> {
				for (E edge : toMap.remove(vertex))
					edges.remove(edge);
			});
			return true;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean changed = false;
			for (Object v : c)
				changed = remove(v) || changed;
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Set<V> remove = createVertexSet();
			remove.addAll(this);
			remove.removeAll(c);

			return removeAll(remove);
		}

		@Override
		public void clear() {
			super.clear();
			edges.clear();
		}

		@Override
		public Set<V> adjacentTo(V vertex) {
			Set<V> adjacent = createVertexSet();
			adjacent.addAll(adjacencyMatrix.get(vertex).keySet());
			return adjacent;
		}

		@Override
		public Set<V> outgoingFrom(V vertex) {
			if (directed) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return adjacentTo(vertex);
		}

		@Override
		public Set<V> incomingTo(V vertex) {
			if (directed) {
				return adjacentToDirectional(vertex, EdgeVertices::getTo);
			} else
				return adjacentTo(vertex);
		}

		private Set<V> adjacentToDirectional(V vertex,
				Function<EdgeVertices<V>, V> edgeToMatch) {
			return adjacencyMatrix
					.get(vertex)
					.entrySet()
					.stream()
					.filter(
							a -> a
									.getValue()
									.stream()
									.filter(
											e -> vertexComparator.compare(
													edgeToMatch.apply(edges.get(e)), vertex) == 0)
									.findAny().isPresent()).map(Map.Entry::getKey)
					.collect(Collectors.toCollection(GraphImpl.this::createVertexSet));
		}

		@Override
		public Comparator<? super V> comparator() {
			return vertexComparator;
		}
	}

	private class EdgesImpl extends MapDecorator<E, EdgeVertices<V>> implements
			Edges<V, E> {
		public EdgesImpl(Comparator<? super E> edgeComparator) {
			super(edgeComparator != null ? new TreeMap<E, EdgeVertices<V>>(
					edgeComparator) : new HashMap<>());
		}

		@Override
		public EdgeVertices<V> remove(Object edge) {
			EdgeVertices<V> vertices = super.remove(edge);

			if (vertices != null)
				adjacencyMatrix.get(vertices.getFrom()).get(vertices.getTo())
						.remove(edge);

			return vertices;
		}

		@Override
		public E add(EdgeVertices<V> vertices) {
			return addEdge.apply(vertices);
		}

		@Override
		public Set<E> adjacentTo(V vertex) {
			return adjacencyMatrix.get(vertex).values().stream().flatMap(Set::stream)
					.collect(Collectors.toCollection(GraphImpl.this::createEdgeSet));
		}

		@Override
		public Set<E> outgoingFrom(V vertex) {
			if (directed) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return adjacentTo(vertex);
		}

		@Override
		public Set<E> incomingTo(V vertex) {
			if (directed) {
				return adjacentToDirectional(vertex, EdgeVertices::getTo);
			} else
				return adjacentTo(vertex);
		}

		private Set<E> adjacentToDirectional(V vertex,
				Function<EdgeVertices<V>, V> edgeToMatch) {
			return adjacencyMatrix
					.get(vertex)
					.entrySet()
					.stream()
					.filter(
							a -> a.getValue().stream()
									.filter(e -> edgeToMatch.apply(edges.get(e)) == vertex)
									.findAny().isPresent()).map(Map.Entry::getValue)
					.flatMap(Set::stream)
					.collect(Collectors.toCollection(GraphImpl.this::createEdgeSet));
		}

		@Override
		public Set<E> between(EdgeVertices<V> vertices) {
			MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(vertices
					.getFrom());
			return adjacencyMap == null ? null : adjacencyMap.get(vertices.getTo());
		}

		@Override
		public E betweenUnique(EdgeVertices<V> vertices) {
			MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(vertices
					.getFrom());
			return adjacencyMap == null ? null : adjacencyMap.get(vertices.getTo())
					.iterator().next();
		}

		@Override
		public double weight(E edge) {
			return edgeWeight.apply(edge);
		}

		@Override
		public Comparator<? super E> comparator() {
			return edgeComparator;
		}
	}

	private final Vertices<V> vertices;
	private final Predicate<V> addVertex;

	private final Comparator<? super V> vertexComparator;
	private final Comparator<? super E> edgeComparator;

	private final Edges<V, E> edges;
	private final Function<EdgeVertices<V>, E> addEdge;

	private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;

	private final boolean directed;
	private final boolean simple;
	private final boolean weighted;
	private final Function<E, Double> edgeWeight;

	public GraphImpl(GraphConfiguratorImpl<V, E> configurator) {
		directed = configurator.isDirected();
		simple = !configurator.isMultigraph();
		weighted = configurator.getEdgeWeight() != null;
		edgeWeight = weighted ? configurator.getEdgeWeight() : e -> 1d;

		vertexComparator = configurator.getVertexComparator();
		edgeComparator = configurator.getEdgeComparator();

		adjacencyMatrix = configurator.getVertexComparator() != null ? new TreeMap<>(
				configurator.getVertexComparator()) : new HashMap<>();
		vertices = new VerticesImpl();
		edges = new EdgesImpl(configurator.getEdgeComparator());

		// Edge addition function
		this.addEdge = addEdgeFunction(configurator.getEdgeFactory(), true);

		// Vertex addition function
		this.addVertex = addVertexPredicate(configurator.getVertexComparator(),
				directed, configurator.getEdgeRule(), configurator.getEdgeGenerator(),
				configurator.getOutgoingEdgeGenerator(),
				configurator.getIncomingEdgeGenerator(), addEdge);

		Function<EdgeVertices<V>, E> addEdgeConstructor = addEdgeFunction(
				configurator.getEdgeFactory(), true);
		Predicate<V> addVertexConstructor = addVertexPredicate(
				configurator.getVertexComparator(), directed,
				configurator.getEdgeRule(), configurator.getEdgeGenerator(),
				configurator.getOutgoingEdgeGenerator(),
				configurator.getIncomingEdgeGenerator(), addEdgeConstructor);

		// Add initial vertices and edges
		if (configurator.getVertices() != null)
			for (V vertex : configurator.getVertices())
				addVertexConstructor.test(vertex);

		if (configurator.getEdgeVertices() != null)
			for (EdgeVertices<V> edge : configurator.getEdgeVertices())
				addEdgeConstructor.apply(edge);
	}

	public Set<V> createVertexSet() {
		return vertexComparator != null ? new TreeSet<>(vertexComparator)
				: new HashSet<>();
	}

	private Set<E> createEdgeSet() {
		return edgeComparator != null ? new TreeSet<>(edgeComparator)
				: new HashSet<>();
	}

	private Function<EdgeVertices<V>, E> addEdgeFunction(
			Function<EdgeVertices<V>, E> edgeFactory, boolean generateNeighbours) {
		return v -> {
			if (simple && adjacencyMatrix.containsKey(v.getFrom())
					&& adjacencyMatrix.get(v.getFrom()).containsKey(v.getTo())) {
				return null;
			} else if (generateNeighbours) {
				addVertex.test(v.getFrom());
				addVertex.test(v.getTo());
			} else if (!adjacencyMatrix.containsKey(v.getFrom())) {
				throw new IllegalArgumentException("Cannot create edge from vertex '"
						+ v.getFrom() + "' as it is not a member of the graph '"
						+ adjacencyMatrix.keySet() + "'");
			} else if (!adjacencyMatrix.containsKey(v.getTo())) {
				throw new IllegalArgumentException("Cannot create edge to vertex '"
						+ v.getTo() + "' as it is not a member of the graph '"
						+ adjacencyMatrix.keySet() + "'");
			}

			E edge = edgeFactory.apply(v);

			adjacencyMatrix.get(v.getFrom()).add(v.getTo(), edge);
			adjacencyMatrix.get(v.getTo()).add(v.getFrom(), edge);
			edges.put(edge, v);

			return edge;
		};
	}

	private Predicate<V> addVertexPredicate(
			Comparator<? super V> vertexComparator, boolean directed,
			BiPredicate<? super V, ? super V> edgeRule,
			Function<? super V, ? extends Collection<? extends V>> edges,
			Function<? super V, ? extends Collection<? extends V>> outgoingEdges,
			Function<? super V, ? extends Collection<? extends V>> incomingEdges,
			Function<EdgeVertices<V>, E> addEdge) {
		Predicate<V> addVertex = vertex -> {
			if (!adjacencyMatrix.containsKey(vertex)) {
				MultiMap<V, E, Set<E>> map;
				if (vertexComparator != null) {
					map = new MultiTreeMap<>(vertexComparator, this::createEdgeSet);
				} else {
					map = new MultiHashMap<>(this::createEdgeSet);
				}
				adjacencyMatrix.put(vertex, map);
				return true;
			}
			return false;
		};

		Function<? super V, ? extends Collection<? extends V>> mergedOutgoingEdges;
		if (outgoingEdges == null) {
			mergedOutgoingEdges = edges;
		} else if (edges == null) {
			mergedOutgoingEdges = outgoingEdges;
		} else {
			mergedOutgoingEdges = v -> {
				List<V> adjacent = new ArrayList<>();
				adjacent.addAll(edges.apply(v));
				adjacent.addAll(outgoingEdges.apply(v));
				return adjacent;
			};
		}
		if (mergedOutgoingEdges != null) {
			addVertex = addVertex.and(vertex -> {
				mergedOutgoingEdges.apply(vertex).stream()
						.map(v -> EdgeVertices.between(vertex, v)).forEach(addEdge::apply);
				return true;
			});
		}

		if (incomingEdges != null) {
			addVertex = addVertex.and(vertex -> {
				incomingEdges.apply(vertex).stream()
						.map(v -> EdgeVertices.between(v, vertex)).forEach(addEdge::apply);
				return true;
			});
		}

		return addVertex;
	}

	@Override
	public Graph<V, E> copy() {
		return new GraphBuilderImpl().configure().readOnly().vertices(vertices())
				.edges(edges.values()).edgeFactory(v -> edges.betweenUnique(v))
				.create();
	}

	@Override
	public Vertices<V> vertices() {
		return vertices;
	}

	@Override
	public Edges<V, E> edges() {
		return edges;
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public boolean isWeighted() {
		return weighted;
	}

	@Override
	public boolean isSimple() {
		return simple;
	}

	@Override
	public GraphTransformer<V, E> transform() {
		return new GraphTransformerImpl<>(this);
	}
}
