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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgeListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgesListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VertexListener;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VerticesListener;
import uk.co.strangeskies.mathematics.graph.GraphTransformer;
import uk.co.strangeskies.utilities.EqualityComparator;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.collection.decorator.SetDecorator;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiTreeMap;

class GraphImpl<V, E> implements Graph<V, E> {
	private class VerticesImpl extends SetDecorator<V> implements Vertices<V, E> {
		public VerticesImpl() {
			super(adjacencyMatrix.keySet());
		}

		@Override
		public boolean add(V e) {
			return addVertex.test(e);
		}

		private boolean addAllImpl(Collection<? extends V> c) {
			boolean changed = false;
			for (V v : c)
				changed = add(v) || changed;
			return changed;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			IdentityProperty<Boolean> added = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<V> vertexSet = createVertexSet();
				vertexSet.addAll(c);
				vertexSet.removeAll(this);

				added.set(vertices.addAllImpl(vertexSet));

				for (VerticesListener<V, E> listener : internalListeners
						.verticesAdded())
					listener.vertices(GraphImpl.this, vertexSet);
			});
			return added.get();
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object vertex) {
			boolean removed = adjacencyMatrix.containsKey(vertex);

			if (removed) {
				atomicInternal(() -> {
					adjacencyMatrix.remove(vertex).values()
							.forEach(s -> s.forEach(e -> edges.remove(e)));

					adjacencyMatrix.values().forEach(toMap -> {
						for (E edge : toMap.remove(vertex))
							edges.remove(edge);
					});

					changeSet.verticesRemoved().add((V) vertex);
					changeSet.verticesAdded().remove(vertex);

					for (VertexListener<V, E> listener : internalListeners
							.vertexRemoved())
						listener.vertex(GraphImpl.this, (V) vertex);
				});
			}

			return removed;
		}

		private boolean removeAllImpl(Collection<?> c) {
			boolean changed = false;
			for (Object v : c)
				changed = remove(v) || changed;
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			IdentityProperty<Boolean> removed = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<V> vertexSet = createVertexSet();
				vertexSet.addAll(this);
				vertexSet.retainAll(c);

				removed.set(vertices.removeAllImpl(vertexSet));

				for (VerticesListener<V, E> listener : internalListeners
						.verticesRemoved())
					listener.vertices(GraphImpl.this, vertexSet);
			});
			return removed.get();
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
		public Set<V> successorsOf(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return adjacentTo(vertex);
		}

		@Override
		public Set<V> predecessorsOf(V vertex) {
			if (isDirected()) {
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
											e -> vertices().equality().test(
													edgeToMatch.apply(incidentTo(e)), vertex)).findAny()
									.isPresent()).map(Map.Entry::getKey)
					.collect(Collectors.toCollection(GraphImpl.this::createVertexSet));
		}

		@Override
		public BiPredicate<? super V, ? super V> equality() {
			return vertexComparator == null ? Objects::equals : vertexComparator;
		}

		@Override
		public Stream<V> stream() {
			return super.stream();
		}

		@Override
		public EdgeVertices<V> incidentTo(E vertex) {
			return edges.edgeVertices.get(vertex);
		}

		@Override
		public V incidentToHead(E vertex) {
			return edges.edgeVertices.get(vertex).getTo();
		}

		@Override
		public V incidentToTail(E vertex) {
			return edges.edgeVertices.get(vertex).getFrom();
		}
	}

	private class EdgesImpl extends SetDecorator<E> implements Edges<V, E> {
		private final Map<E, EdgeVertices<V>> edgeVertices;

		public EdgesImpl(BiPredicate<? super E, ? super E> edgeComparator) {
			this(edgeComparator != null ? new TreeMap<>(new EqualityComparator<E>(
					edgeComparator)) : new HashMap<>());
		}

		private EdgesImpl(Map<E, EdgeVertices<V>> edgeVertices) {
			super(edgeVertices.keySet());

			this.edgeVertices = edgeVertices;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object edge) {
			boolean removed = super.remove(edge);

			if (removed) {
				atomicInternal(() -> {
					EdgeVertices<V> vertices = edges.edgeVertices.get(edge);

					adjacencyMatrix.get(vertices.getFrom()).get(vertices.getTo())
							.remove(edge);

					if (!changeSet.edgesRemoved().containsKey(edge))
						changeSet.edgesRemoved().put((E) edge, vertices);
					changeSet.edgesAdded().remove(edge);

					for (EdgeListener<V, E> listener : internalListeners.edgeAdded())
						listener.edge(GraphImpl.this, (E) edge, vertices);
				});
			}

			return removed;
		}

		@Override
		public E add(EdgeVertices<V> vertices) {
			return addInferredEdge.apply(vertices);
		}

		private boolean addAllImpl(Collection<? extends E> c) {
			boolean changed = false;

			for (E edge : c)
				changed = add(edge) | changed;

			return changed;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			IdentityProperty<Boolean> added = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<E> edgeSet = createEdgeSet();
				edgeSet.addAll(c);
				edgeSet.removeAll(this);

				added.set(edges.addAllImpl(edgeSet));

				for (EdgesListener<V, E> listener : internalListeners.edgesAdded())
					listener.edges(
							GraphImpl.this,
							edgeSet.stream().collect(
									Collectors.toMap(Function.identity(), vertices::incidentTo)));
			});
			return added.get();
		}

		private boolean removeAllImpl(Collection<?> c) {
			boolean changed = false;

			for (Object edge : c)
				changed = remove(edge) | changed;

			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			IdentityProperty<Boolean> removed = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<E> edgeSet = createEdgeSet();
				edgeSet.addAll(this);
				edgeSet.retainAll(c);

				removed.set(edges.removeAllImpl(edgeSet));

				for (EdgesListener<V, E> listener : internalListeners.edgesRemoved())
					listener.edges(
							GraphImpl.this,
							edgeSet.stream().collect(
									Collectors.toMap(Function.identity(), vertices::incidentTo)));
			});
			return removed.get();
		}

		@Override
		public Set<E> incidentTo(V vertex) {
			return adjacencyMatrix.get(vertex).values().stream().flatMap(Set::stream)
					.collect(Collectors.toCollection(GraphImpl.this::createEdgeSet));
		}

		@Override
		public Set<E> incidentToHead(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getTo);
			} else
				return incidentTo(vertex);
		}

		@Override
		public Set<E> incidentToTail(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return incidentTo(vertex);
		}

		private Set<E> adjacentToDirectional(V vertex,
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
											e -> edgeToMatch.apply(vertices.incidentTo(e)) == vertex)
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
		public BiPredicate<? super E, ? super E> equality() {
			return edgeComparator == null ? Objects::equals : edgeComparator;
		}

		private void put(E edge, EdgeVertices<V> vertices) {
			edgeVertices.put(edge, vertices);
		}

		@Override
		public Set<EdgeVertices<V>> edgeVertices() {
			return stream().map(vertices()::incidentTo).collect(Collectors.toSet());
		}
	}

	private final VerticesImpl vertices;
	private final Predicate<V> addVertex;

	private final BiPredicate<? super V, ? super V> vertexComparator;
	private final BiPredicate<? super E, ? super E> edgeComparator;

	private final EdgesImpl edges;
	private final Function<EdgeVertices<V>, E> addInferredEdge;
	private final BiConsumer<E, EdgeVertices<V>> addEdge;

	private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;

	private final Function<E, Comparator<V>> lowToHighDirectionFunction;
	private final Comparator<V> lowToHighDirection;
	private final boolean simple;
	private final boolean weighted;
	private final Function<E, Double> edgeWeight;

	private final GraphListeners<V, E> internalListeners;
	private final GraphListeners<V, E> listeners;

	private ChangeSetImpl<V, E> changeSet;

	public GraphImpl(GraphConfiguratorImpl<V, E> configurator) {
		listeners = new GraphListeners<>();
		internalListeners = configurator.getInternalListeners();

		lowToHighDirection = configurator.getLowToHighDirection();
		lowToHighDirectionFunction = configurator.getLowToHighDirectionFunction();
		simple = !configurator.isMultigraph();
		weighted = configurator.getEdgeWeight() != null;
		edgeWeight = weighted ? configurator.getEdgeWeight() : e -> 1d;

		vertexComparator = configurator.getVertexEquality();
		edgeComparator = configurator.getEdgeEquality();

		adjacencyMatrix = createVertexMap();
		vertices = new VerticesImpl();
		edges = new EdgesImpl(configurator.getEdgeEquality());

		// Edge addition function
		this.addEdge = addEdgeConsumer();
		this.addInferredEdge = addInferredEdgeFunction(
				configurator.getEdgeFactory(), this.addEdge);

		// Vertex addition function
		this.addVertex = addVertexPredicate(configurator.getVertexEquality(),
				addInferredEdge);

		Function<EdgeVertices<V>, E> addEdgeConstructor = addInferredEdgeFunction(
				configurator.getEdgeFactory(), addEdgeConsumer());
		Predicate<V> addVertexConstructor = addVertexPredicate(
				configurator.getVertexEquality(), addEdgeConstructor);

		// Add initial vertices and edges
		if (configurator.getVertices() != null)
			for (V vertex : configurator.getVertices())
				addVertexConstructor.test(vertex);

		if (configurator.getEdgeVertices() != null)
			for (EdgeVertices<V> edge : configurator.getEdgeVertices())
				addEdgeConstructor.apply(edge);

		if (configurator.getEdgeMap() != null)
			for (Map.Entry<E, EdgeVertices<V>> edge : configurator.getEdgeMap()
					.entrySet())
				addEdge.accept(edge.getKey(), edge.getValue());
	}

	private Function<EdgeVertices<V>, E> addInferredEdgeFunction(
			Function<EdgeVertices<V>, E> edgeFactory,
			BiConsumer<E, EdgeVertices<V>> addEdgeConsumer) {
		if (edgeFactory != null) {
			return v -> {
				if (simple && adjacencyMatrix.containsKey(v.getFrom())
						&& adjacencyMatrix.get(v.getFrom()).containsKey(v.getTo())) {
					return null;
				}

				if (lowToHighDirection != null)
					v = EdgeVertices.between(v.getFrom(), v.getTo(), lowToHighDirection);

				E edge = edgeFactory.apply(v);

				addEdgeConsumer.accept(edge, v);

				return edge;
			};
		} else {
			return v -> {
				throw new UnsupportedOperationException(
						"Edge instance cannot be inferred, and must be explicitly provided");
			};
		}
	}

	private BiConsumer<E, EdgeVertices<V>> addEdgeConsumer() {
		return (e, v) -> {
			if (simple && adjacencyMatrix.containsKey(v.getFrom())
					&& adjacencyMatrix.get(v.getFrom()).containsKey(v.getTo())) {
				return;
			} else if (!adjacencyMatrix.containsKey(v.getFrom())) {
				throw new IllegalArgumentException("Cannot create edge from vertex '"
						+ v.getFrom() + "' as it is not a member of the graph '"
						+ adjacencyMatrix.keySet() + "'");
			} else if (!adjacencyMatrix.containsKey(v.getTo())) {
				throw new IllegalArgumentException("Cannot create edge to vertex '"
						+ v.getTo() + "' as it is not a member of the graph '"
						+ adjacencyMatrix.keySet() + "'");
			}

			EdgeVertices<V> vertex;
			if (lowToHighDirectionFunction != null)
				vertex = EdgeVertices.between(v.getFrom(), v.getTo(),
						lowToHighDirectionFunction.apply(e));
			else
				vertex = v;

			atomicInternal(() -> {
				adjacencyMatrix.get(vertex.getFrom()).add(vertex.getTo(), e);
				adjacencyMatrix.get(vertex.getTo()).add(vertex.getFrom(), e);
				edges.put(e, vertex);

				changeSet.edgesRemoved().remove(e, vertex);
				changeSet.edgesAdded().put(e, vertex);

				for (EdgeListener<V, E> listener : internalListeners.edgeAdded())
					listener.edge(GraphImpl.this, e, vertex);
			});
		};
	}

	private Predicate<V> addVertexPredicate(
			BiPredicate<? super V, ? super V> vertexComparator,
			Function<EdgeVertices<V>, E> addEdge) {
		Predicate<V> addVertex = vertex -> {
			if (!adjacencyMatrix.containsKey(vertex)) {
				MultiMap<V, E, Set<E>> map;
				if (vertexComparator != null) {
					map = new MultiTreeMap<>(new EqualityComparator<V>(vertexComparator),
							this::createEdgeSet);
				} else {
					map = new MultiHashMap<>(this::createEdgeSet);
				}

				atomicInternal(() -> {
					adjacencyMatrix.put(vertex, map);

					changeSet.verticesRemoved().remove(vertex);
					changeSet.verticesAdded().add(vertex);

					for (VertexListener<V, E> listener : internalListeners.vertexAdded())
						listener.vertex(GraphImpl.this, vertex);
				});

				return true;
			}
			return false;
		};

		return addVertex;
	}

	@Override
	public Graph<V, E> copy() {
		return new GraphBuilderImpl().configure().readOnly().addVertices(vertices)
				.addEdges(edges.edgeVertices).edgeFactory(v -> edges.betweenUnique(v))
				.create();
	}

	@Override
	public Vertices<V, E> vertices() {
		return vertices;
	}

	@Override
	public Edges<V, E> edges() {
		return edges;
	}

	@Override
	public boolean isDirected() {
		return lowToHighDirection != null || lowToHighDirectionFunction != null;
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

	@Override
	public GraphListeners<V, E> listeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void atomic(Consumer<? super Graph<V, E>> action) {
		atomicInternal(() -> action.accept(this));
	}

	private void atomicInternal(Runnable action) {
		if (changeSet == null) {
			changeSet = new ChangeSetImpl<>(this);

			try {
				action.run();

				listeners.change().forEach(c -> c.change(this, changeSet.copy()));
			} catch (Exception e) {
				/*
				 * TODO discard change set
				 */

				changeSet = null;

				throw e;
			}

			changeSet.tryTriggerListeners(listeners);

			changeSet = null;
		} else {
			action.run();
		}
	}

	@Override
	public Set<V> createVertexSet() {
		return vertexComparator != null ? new TreeSet<>(new EqualityComparator<V>(
				vertexComparator)) : new HashSet<>();
	}

	@Override
	public Set<V> createVertexSet(Collection<? extends V> vertices) {
		if (vertexComparator != null) {
			Set<V> vertexSet = new TreeSet<>(new EqualityComparator<V>(
					vertexComparator));
			vertexSet.addAll(vertices);
			return vertexSet;
		} else
			return new HashSet<>(vertices);
	}

	@Override
	public <T> Map<V, T> createVertexMap() {
		return vertexComparator != null ? new TreeMap<>(new EqualityComparator<V>(
				vertexComparator)) : new HashMap<>();
	}

	@Override
	public <T> Map<V, T> createVertexMap(Map<? extends V, ? extends T> edges) {
		if (vertexComparator != null) {
			Map<V, T> edgeMap = new TreeMap<>(new EqualityComparator<V>(
					vertexComparator));
			edgeMap.putAll(edges);
			return edgeMap;
		} else
			return new HashMap<>(edges);
	}

	@Override
	public Set<E> createEdgeSet() {
		return edgeComparator != null ? new TreeSet<>(new EqualityComparator<E>(
				edgeComparator)) : new HashSet<>();
	}

	@Override
	public Set<E> createEdgeSet(Collection<? extends E> edges) {
		if (edgeComparator != null) {
			Set<E> edgeSet = new TreeSet<>(new EqualityComparator<E>(edgeComparator));
			edgeSet.addAll(edges);
			return edgeSet;
		} else
			return new HashSet<>(edges);
	}

	@Override
	public <T> Map<E, T> createEdgeMap() {
		return edgeComparator != null ? new TreeMap<>(new EqualityComparator<E>(
				edgeComparator)) : new HashMap<>();
	}

	@Override
	public <T> Map<E, T> createEdgeMap(Map<? extends E, ? extends T> edges) {
		if (edgeComparator != null) {
			Map<E, T> edgeMap = new TreeMap<>(new EqualityComparator<E>(
					edgeComparator));
			edgeMap.putAll(edges);
			return edgeMap;
		} else
			return new HashMap<>(edges);
	}
}
