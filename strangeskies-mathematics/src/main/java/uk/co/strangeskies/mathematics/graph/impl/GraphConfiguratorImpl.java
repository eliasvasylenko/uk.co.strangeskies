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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphTransformer;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.collection.MultiTreeMap;
import uk.co.strangeskies.utilities.collection.SetDecorator;
import uk.co.strangeskies.utilities.factory.Configurator;
import uk.co.strangeskies.utilities.function.Functions;

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
				Set<V> remove = new TreeSet<>(comparator());
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
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<V> outgoingFrom(V vertex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<V> incomingTo(V vertex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Comparator<V> comparator() {
				return comparator;
			}
		}

		private class EdgesImpl extends TreeMap<E, EdgeVertices<V>> implements
				Edges<E, V> {
			private static final long serialVersionUID = 1L;

			public EdgesImpl() {
				super(new IdentityComparator<>());
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
			public E add(V from, V to) {
				return addEdge.apply(from, to);
			}

			@Override
			public Set<E> adjacentTo(V vertex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<E> outgoingFrom(V vertex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<E> incomingTo(V vertex) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<E> between(V from, V to) {
				MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(from);
				return adjacencyMap == null ? null : adjacencyMap.get(to);
			}

			@Override
			public E betweenUnique(V from, V to) {
				MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(from);
				return adjacencyMap == null ? null : adjacencyMap.get(to).iterator()
						.next();
			}

			@Override
			public double weight(E edge) {
				return edgeWeight.apply(edge);
			}
		}

		private final boolean directed;
		private final boolean simple;
		private final boolean weighted;
		private final Function<E, Double> edgeWeight;

		private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;
		private final Comparator<V> comparator;
		private final Vertices<V> vertices;
		private final Edges<E, V> edges;

		private final Predicate<V> addVertex;
		private final BiFunction<V, V, E> addEdge;

		public GraphImpl(GraphConfiguratorImpl<V, E> configurator) {
			directed = configurator.directed;
			simple = !configurator.multigraph;
			weighted = configurator.edgeWeight != null;
			edgeWeight = weighted ? configurator.edgeWeight : e -> 1d;

			comparator = configurator.comparator != null ? configurator.comparator
					: new IdentityComparator<>();
			adjacencyMatrix = configurator.comparator != null ? new TreeMap<>(
					comparator) : new HashMap<>();
			vertices = new VerticesImpl();
			edges = new EdgesImpl();

			Predicate<V> addVertex = addVertexPredicate(comparator,
					configurator.edgeGenerator, configurator.outgoingEdgeGenerator,
					configurator.incomingEdgeGenerator);
			this.addVertex = configurator.unmodifiableVertices ? v -> {
				throw new UnsupportedOperationException();
			} : addVertex;

			if (configurator.vertices != null)
				for (V vertex : configurator.vertices)
					addVertex.test(vertex);

			BiFunction<V, V, E> addEdge = addEdgePredicate(configurator.edgeFactory,
					configurator.generateNeighbours);
			this.addEdge = configurator.unmodifiableEdges ? (f, t) -> {
				throw new UnsupportedOperationException();
			} : addEdge;

			if (configurator.edges != null)
				for (EdgeVertices<V> edge : configurator.edges)
					addEdge.apply(edge.getFrom(), edge.getTo());
		}

		private BiFunction<V, V, E> addEdgePredicate(
				BiFunction<V, V, E> edgeFactory, boolean generateNeighbours) {
			return (from, to) -> {
				if (generateNeighbours) {
					vertices().add(from);
					vertices().add(to);
				} else if (!adjacencyMatrix.containsKey(from)
						|| !adjacencyMatrix.containsKey(to))
					throw new IllegalArgumentException();

				E edge = edgeFactory.apply(from, to);
				EdgeVertices<V> vertices = EdgeVertices.between(from, to);

				adjacencyMatrix.get(from).add(to, edge); // TODO simple/multi
				edges.put(edge, vertices);

				return edge;
			};
		}

		private Predicate<V> addVertexPredicate(Comparator<V> comparator,
				Function<? super V, ? extends Collection<? extends V>> edges,
				Function<? super V, ? extends Collection<? extends V>> outgoingEdges,
				Function<? super V, ? extends Collection<? extends V>> incomingEdges) {
			Predicate<V> addVertex = vertex -> {
				if (!adjacencyMatrix.containsKey(vertex)) {
					adjacencyMatrix.put(vertex, comparator != null ? new MultiTreeMap<>(
							comparator, TreeSet::new) : new MultiHashMap<>(TreeSet::new));
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
					edges().add(
							mergedOutgoingEdges.apply(vertex).stream()
									.map(v -> EdgeVertices.between(vertex, v))
									.collect(Collectors.toSet()));
					return true;
				});
			}

			if (incomingEdges != null) {
				addVertex = addVertex.and(vertex -> {
					edges().add(
							incomingEdges.apply(vertex).stream()
									.map(v -> EdgeVertices.between(v, vertex))
									.collect(Collectors.toSet()));
					return true;
				});
			}

			return addVertex;
		}

		@Override
		public Graph<V, E> copy() {
			return new GraphBuilderImpl().configure().unmodifiableStructure()
					.vertices(vertices()).edges(edges.values())
					.edgeFactory((v, w) -> edges.betweenUnique(v, w)).create();
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

	private boolean unmodifiableVertices;
	private boolean unmodifiableEdges;
	private Collection<V> vertices;
	private Set<EdgeVertices<V>> edges;
	private boolean directed;
	private boolean acyclic;
	private boolean multigraph;
	private Function<E, Comparator<V>> lowToHighDirection;
	private BiFunction<V, V, E> edgeFactory;
	private BiFunction<V, V, ? extends Set<? extends E>> edgeMultiFactory;
	private Function<E, Double> edgeWeight;
	private boolean edgeWeightMutable;
	private Function<? super V, ? extends Collection<? extends V>> incomingEdgeGenerator;
	private Function<? super V, ? extends Collection<? extends V>> outgoingEdgeGenerator;
	private Function<? super V, ? extends Collection<? extends V>> edgeGenerator;
	private BiPredicate<? super V, ? super V> edgeRule;
	private boolean generateNeighbours;
	private Predicate<Graph<V, E>> constraint;
	private Comparator<V> comparator;

	protected static GraphConfigurator<Object, Object> configure() {
		return new GraphConfiguratorImpl<>().edgeFactory(() -> new Object())
				.comparator(new IdentityComparator<>());
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
	public <W extends Expression<? extends V>> GraphConfigurator<W, E> verticesAsExpressions(
			Collection<W> vertices) {
		assertConfigurable(this.vertices, incomingEdgeGenerator,
				outgoingEdgeGenerator, edgeGenerator);

		this.vertices = (Set<V>) new HashSet<W>(vertices);

		edgeFactory = expressionForward(edgeFactory);

		edgeRule = expressionForward(edgeRule);

		return (GraphConfigurator<W, E>) this;
	}

	@SuppressWarnings("unchecked")
	private <W> BiPredicate<W, W> expressionForward(
			BiPredicate<? super W, ? super W> function) {
		return function == null ? null : (BiPredicate<W, W>) Functions
				.<Expression<? extends W>, W> compose(v -> v.getValue(), function);
	}

	@SuppressWarnings("unchecked")
	private <W, T> BiFunction<W, W, T> expressionForward(
			BiFunction<? super W, ? super W, T> function) {
		return function == null ? null : (BiFunction<W, W, T>) Functions
				.<Expression<? extends W>, W, T> compose(v -> v.getValue(), function);
	}

	@Override
	public GraphConfigurator<V, E> edges(Collection<EdgeVertices<V>> edges) {
		assertConfigurable(this.edges);
		this.edges = new HashSet<>(edges);
		return this;
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
			BiFunction<V, V, F> factory) {
		assertConfigurable(edgeFactory, edgeMultiFactory);
		edgeFactory = (BiFunction<V, V, E>) factory;
		return (GraphConfigurator<V, F>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edgeMultiFactory(
			BiFunction<V, V, Set<F>> factory) {
		assertConfigurable(edgeFactory, edgeMultiFactory);
		edgeMultiFactory = (BiFunction<V, V, Set<E>>) (Object) factory;
		return (GraphConfigurator<V, F>) multigraph();
	}

	@Override
	public GraphConfigurator<V, E> edgeWeight(Function<E, Double> weight,
			boolean mutable) {
		assertConfigurable(edgeWeight);
		edgeWeight = weight;
		edgeWeightMutable = mutable;
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
	public GraphConfigurator<V, E> comparator(Comparator<V> comparator) {
		assertConfigurable(this.comparator);
		this.comparator = comparator;
		return this;
	}
}
