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
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.collection.decorator.MapDecorator;
import uk.co.strangeskies.utilities.collection.decorator.SetDecorator;
import uk.co.strangeskies.utilities.collection.multimap.MultiHashMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiMap;
import uk.co.strangeskies.utilities.collection.multimap.MultiTreeMap;
import uk.co.strangeskies.utilities.factory.Configurator;

public class GraphConfiguratorImpl<V, E> extends Configurator<Graph<V, E>>
		implements GraphConfigurator<V, E> {
	private static class GraphImpl<V, E> implements Graph<V, E> {
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
								a -> a.getValue().stream()
										.filter(e -> edgeToMatch.apply(edges.get(e)) == vertex)
										.findAny().isPresent()).map(Map.Entry::getKey)
						.collect(Collectors.toCollection(GraphImpl.this::createVertexSet));
			}

			@Override
			public Comparator<? super V> comparator() {
				return vertexComparator;
			}
		}

		private class EdgesImpl extends MapDecorator<E, EdgeVertices<V>> implements
				Edges<E, V> {
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
				return adjacencyMatrix.get(vertex).values().stream()
						.flatMap(Set::stream)
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

		private final Edges<E, V> edges;
		private final Function<EdgeVertices<V>, E> addEdge;

		private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;

		private final boolean directed;
		private final boolean simple;
		private final boolean weighted;
		private final Function<E, Double> edgeWeight;

		public GraphImpl(GraphConfiguratorImpl<V, E> configurator) {
			directed = configurator.directed;
			simple = !configurator.multigraph;
			weighted = configurator.edgeWeight != null;
			edgeWeight = weighted ? configurator.edgeWeight : e -> 1d;

			vertexComparator = configurator.vertexComparator;
			edgeComparator = configurator.edgeComparator;

			adjacencyMatrix = configurator.vertexComparator != null ? new TreeMap<>(
					configurator.vertexComparator) : new HashMap<>();
			vertices = new VerticesImpl();
			edges = new EdgesImpl(configurator.edgeComparator);

			// Vertex addition function
			this.addVertex = addVertexPredicate(configurator.vertexComparator,
					configurator.edgeGenerator, configurator.outgoingEdgeGenerator,
					configurator.incomingEdgeGenerator);

			// Edge addition function
			this.addEdge = addEdgeFunction(configurator.edgeFactory,
					configurator.generateNeighbours);

			// Add initial vertices and edges
			if (configurator.vertices != null)
				for (V vertex : configurator.vertices)
					addVertex.test(vertex);

			if (configurator.edgeVertices != null)
				for (EdgeVertices<V> edge : configurator.edgeVertices)
					addEdge.apply(edge);
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
				} else if (!adjacencyMatrix.containsKey(v.getFrom())
						|| !adjacencyMatrix.containsKey(v.getTo()))
					throw new IllegalArgumentException();

				E edge = edgeFactory.apply(v);

				adjacencyMatrix.get(v.getFrom()).add(v.getTo(), edge);
				adjacencyMatrix.get(v.getTo()).add(v.getFrom(), edge);
				edges.put(edge, v);

				return edge;
			};
		}

		private Predicate<V> addVertexPredicate(
				Comparator<? super V> vertexComparator,
				Function<? super V, ? extends Collection<? extends V>> edges,
				Function<? super V, ? extends Collection<? extends V>> outgoingEdges,
				Function<? super V, ? extends Collection<? extends V>> incomingEdges) {
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
							.map(v -> EdgeVertices.between(vertex, v))
							.forEach(addEdge::apply);
					return true;
				});
			}

			if (incomingEdges != null) {
				addVertex = addVertex.and(vertex -> {
					incomingEdges.apply(vertex).stream()
							.map(v -> EdgeVertices.between(v, vertex))
							.forEach(addEdge::apply);
					return true;
				});
			}

			return addVertex;
		}

		@Override
		public Graph<V, E> copy() {
			return new GraphBuilderImpl().configure().unmodifiableStructure()
					.vertices(vertices()).edgeVertices(edges.values())
					.edgeFactory(v -> edges.betweenUnique(v)).create();
		}

		@Override
		public Vertices<V> vertices() {
			return vertices;
		}

		@Override
		public Edges<E, V> edges() {
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

	private Collection<V> vertices;
	private boolean unmodifiableVertices;
	private Comparator<? super V> vertexComparator;

	private Set<EdgeVertices<V>> edgeVertices;
	private Set<? extends E> edges;
	private Map<E, EdgeVertices<V>> edgeMap;
	private boolean unmodifiableEdges;
	private Comparator<? super E> edgeComparator;

	private boolean directed;
	private boolean acyclic;
	private boolean multigraph;

	private Function<E, Comparator<V>> lowToHighDirection;
	private Function<EdgeVertices<V>, E> edgeFactory;
	private Function<EdgeVertices<V>, ? extends Set<? extends E>> edgeMultiFactory;
	private Function<E, Double> edgeWeight;

	private Function<? super V, ? extends Collection<? extends V>> incomingEdgeGenerator;
	private Function<? super V, ? extends Collection<? extends V>> outgoingEdgeGenerator;
	private Function<? super V, ? extends Collection<? extends V>> edgeGenerator;
	private BiPredicate<? super V, ? super V> edgeRule;
	private boolean generateNeighbours;

	private Predicate<Graph<V, E>> constraint;

	protected static GraphConfigurator<Object, Object> configure() {
		return new GraphConfiguratorImpl<>().edgeFactory(() -> new Object())
				.vertexComparator(new IdentityComparator<>());
	}

	@Override
	public Graph<V, E> tryCreate() {
		return new GraphImpl<V, E>(this);
	}

	@Override
	public GraphConfigurator<V, E> unmodifiableStructure() {
		return unmodifiableVertices().unmodifiableEdges();
	}

	@Override
	public GraphConfigurator<V, E> unmodifiableVertices() {
		unmodifiableVertices = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> unmodifiableEdges() {
		unmodifiableEdges = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <W extends V> GraphConfigurator<W, E> vertices(Collection<W> vertices) {
		assertConfigurable(this.vertices);
		this.vertices = (Collection<V>) vertices;
		return (GraphConfigurator<W, E>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GraphConfigurator<V, E> edgeVertices(
			Collection<? extends EdgeVertices<V>> edges) {
		assertConfigurable(edgeVertices);
		assertConfigurable(this.edges);
		assertConfigurable(edgeMap);

		this.edgeVertices = (Set<EdgeVertices<V>>) edges;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> edges(Map<E, EdgeVertices<V>> edges) {
		assertConfigurable(edgeVertices);
		assertConfigurable(this.edges);
		assertConfigurable(edgeMap);

		this.edgeMap = edges;
		return this;
	}

	@Override
	public <F extends E> GraphConfigurator<V, F> edges(
			Collection<? extends F> edges) {
		assertConfigurable(edgeVertices);
		assertConfigurable(this.edges);
		assertConfigurable(edgeMap);

		this.edges = (Set<? extends E>) edges;
		return null;
	}

	@Override
	public GraphConfigurator<V, E> edgeVerticesFunction(
			Function<E, EdgeVertices<V>> factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphConfigurator<V, E> edgesBetween(
			Function<? super V, ? extends Collection<? extends V>> neighbours) {
		assertConfigurable(this.edgeGenerator);
		edgeGenerator = neighbours;
		return unmodifiableEdges();
	}

	@Override
	public GraphConfigurator<V, E> edgesFrom(
			Function<? super V, ? extends Collection<? extends V>> fromNeighbours) {
		assertConfigurable(this.incomingEdgeGenerator);
		incomingEdgeGenerator = fromNeighbours;
		directed = true;
		return unmodifiableEdges();
	}

	@Override
	public GraphConfigurator<V, E> edgesTo(
			Function<? super V, ? extends Collection<? extends V>> toNeighbours) {
		assertConfigurable(this.outgoingEdgeGenerator);
		outgoingEdgeGenerator = toNeighbours;
		directed = true;
		return unmodifiableEdges();
	}

	@Override
	public GraphConfigurator<V, E> edgeRule(
			BiPredicate<? super V, ? super V> neighbours) {
		assertConfigurable(this.edgeRule);
		edgeRule = neighbours;
		return unmodifiableEdges();
	}

	@Override
	public GraphConfigurator<V, E> acyclic() {
		acyclic = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> multigraph() {
		multigraph = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> direction(Function<E, Comparator<V>> lowToHigh) {
		assertConfigurable(this.lowToHighDirection);
		lowToHighDirection = lowToHigh;
		directed = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edgeFactory(
			Function<EdgeVertices<V>, F> factory) {
		assertConfigurable(edgeFactory, edgeMultiFactory);
		edgeFactory = (Function<EdgeVertices<V>, E>) factory;
		return (GraphConfigurator<V, F>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edgeMultiFactory(
			Function<EdgeVertices<V>, Set<F>> factory) {
		assertConfigurable(edgeFactory, edgeMultiFactory);
		edgeMultiFactory = (Function<EdgeVertices<V>, Set<E>>) (Object) factory;
		return (GraphConfigurator<V, F>) multigraph();
	}

	@Override
	public GraphConfigurator<V, E> edgeWeight(Function<E, Double> weight) {
		assertConfigurable(edgeWeight);
		edgeWeight = weight;

		return this;
	}

	@Override
	public GraphConfigurator<V, E> generateNeighbours() {
		generateNeighbours = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> constrain(Predicate<Graph<V, E>> constraint) {
		assertConfigurable(this.constraint);
		this.constraint = constraint;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> vertexComparator(
			Comparator<? super V> comparator) {
		assertConfigurable(this.vertexComparator);
		this.vertexComparator = comparator;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> edgeComparator(Comparator<? super E> comparator) {
		assertConfigurable(this.edgeComparator);
		this.edgeComparator = comparator;
		return this;
	}
}
