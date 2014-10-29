package uk.co.strangeskies.mathematics.graph.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphTransformer;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.collection.MultiHashMap;
import uk.co.strangeskies.utilities.collection.MultiMap;
import uk.co.strangeskies.utilities.collection.MultiTreeMap;
import uk.co.strangeskies.utilities.factory.Configurator;
import uk.co.strangeskies.utilities.function.Functions;

public class GraphConfiguratorImpl<V, E> extends Configurator<Graph<V, E>>
		implements GraphConfigurator<V, E> {
	private static class ModifiableGraphImpl<V, E> implements Graph<V, E> {
		private final boolean directed;
		private final boolean simple;
		private final boolean weighted;
		private final Function<E, Double> edgeWeight;

		private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;
		private final Map<E, EdgeVertices<V>> edgeList;

		private final Predicate<V> addVertex;
		private final BiFunction<V, V, E> addEdge;

		public ModifiableGraphImpl(GraphConfiguratorImpl<V, E> configurator) {
			directed = configurator.directed;
			simple = !configurator.multigraph;
			weighted = configurator.edgeWeight != null;
			edgeWeight = weighted ? configurator.edgeWeight : e -> 1d;

			Comparator<V> comparator = configurator.comparator;
			adjacencyMatrix = comparator != null ? new TreeMap<>(comparator)
					: new HashMap<>();
			edgeList = new TreeMap<>(new IdentityComparator<>());

			Predicate<V> addVertex = vertex -> {
				if (!adjacencyMatrix.containsKey(vertex)) {
					adjacencyMatrix.put(vertex, comparator != null ? new MultiTreeMap<>(
							comparator, TreeSet::new) : new MultiHashMap<>(TreeSet::new));
					return true;
				}
				return false;
			};
			boolean generateNeighbours = configurator.generateNeighbours;
			if (configurator.edgeGenerator != null) {
				Function<? super V, ? extends Collection<? extends V>> edgeGenerator = configurator.edgeGenerator;
				addVertex = addVertex.and(vertex -> {
					addEdges(vertex, edgeGenerator.apply(vertex), generateNeighbours);
					return true;
				});
			}
			if (configurator.incomingEdgeGenerator != null) {
				Function<? super V, ? extends Collection<? extends V>> edgeGenerator = configurator.incomingEdgeGenerator;
				addVertex = addVertex.and(vertex -> {
					addEdges(edgeGenerator.apply(vertex), vertex, generateNeighbours);
					return true;
				});
			}
			if (configurator.outgoingEdgeGenerator != null) {
				Function<? super V, ? extends Collection<? extends V>> edgeGenerator = configurator.outgoingEdgeGenerator;
				addVertex = addVertex.and(vertex -> {
					addEdges(vertex, edgeGenerator.apply(vertex), generateNeighbours);
					return true;
				});
			}

			if (configurator.vertices != null)
				for (V vertex : configurator.vertices)
					addVertex.test(vertex);

			this.addVertex = configurator.unmodifiableVertices ? Graph.super::addVertex
					: addVertex;

			BiFunction<V, V, E> edgeFactory = configurator.edgeFactory;
			BiFunction<V, V, E> addEdge = (from, to) -> {
				if (!adjacencyMatrix.containsKey(from)
						|| !adjacencyMatrix.containsKey(to))
					throw new IllegalArgumentException();

				E edge = edgeFactory.apply(from, to);
				EdgeVertices<V> vertices = new EdgeVertices<V>(from, to);

				adjacencyMatrix.get(from).put(to, edge);
				edgeList.put(edge, vertices);

				return edge;
			};

			if (configurator.edges != null)
				for (EdgeVertices<V> edge : configurator.edges)
					addEdge.apply(edge.getFrom(), edge.getTo());

			this.addEdge = configurator.unmodifiableEdges ? Graph.super::addEdge
					: addEdge;
		}

		@Override
		public Graph<V, E> copy() {
			return new GraphBuilderImpl().configure().unmodifiableStructure()
					.vertices(getVertices()).edges(getEdgeVertices())
					.edgeFactory((v, w) -> getEdge(v, w)).create();
		}

		@Override
		public Set<V> getVertices() {
			return adjacencyMatrix.keySet();
		}

		@Override
		public Set<E> getEdges() {
			return edgeList.keySet();
		}

		@Override
		public E getEdge(V from, V to) {
			Map<V, E> adjacencyMap = adjacencyMatrix.get(from);
			return adjacencyMap == null ? null : adjacencyMap.get(to);
		}

		@Override
		public EdgeVertices<V> getVertices(E edge) {
			return edgeList.get(edge);
		}

		@Override
		public boolean addVertex(V vertex) {
			return addVertex.test(vertex);
		}

		@Override
		public boolean removeVertex(V vertex) {
			if (adjacencyMatrix.remove(vertex) == null)
				return false;

			adjacencyMatrix.values().forEach(v -> edgeList.remove(v.remove(vertex)));
			return true;
		}

		@Override
		public E addEdge(V from, V to) {
			return addEdge.apply(from, to);
		}

		@Override
		public E removeEdge(V from, V to) {
			E edge = adjacencyMatrix.get(from).remove(to);
			edgeList.remove(edge);

			return edge;
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
		public double weight(E edge) {
			return edgeWeight.apply(edge);
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
		return new ModifiableGraphImpl<V, E>(this);
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
