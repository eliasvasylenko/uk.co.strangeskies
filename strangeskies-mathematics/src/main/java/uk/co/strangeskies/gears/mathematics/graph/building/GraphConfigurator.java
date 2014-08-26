package uk.co.strangeskies.gears.mathematics.graph.building;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.gears.mathematics.graph.Graph;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public interface GraphConfigurator<V, E> extends Factory<Graph<V, E>> {
	/**
	 * Calling this method has the same effect as calling both
	 * {@link #unmodifiableVertices()} and {@link #unmodifiableEdges()}.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> unmodifiableStructure();

	/**
	 * Calling this method has the effect of making the resulting graph
	 * unmodifiable through the manual addition of vertices.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> unmodifiableVertices();

	/**
	 * Calling this method has the effect of making the resulting graph
	 * unmodifiable through the manual addition of edges.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> unmodifiableEdges();

	/**
	 * Accepts a collection of vertices to be contained in the resulting graph.
	 *
	 * @param vertices
	 * @return
	 */
	public <W extends V> GraphConfigurator<W, E> vertices(Collection<W> vertices);

	/**
	 * This method wraps and forwards it's parameters to
	 * {@link #vertices(Collection)}.
	 *
	 * @param vertices
	 * @return
	 */
	public default <W extends V> GraphConfigurator<W, E> vertices(
			@SuppressWarnings("unchecked") W... vertices) {
		return vertices(Arrays.asList(vertices));
	}

	/**
	 * This method behaves as {@link #vertices(Collection)} with the addition that
	 * the {@link Expression} objects which are added as vertices will be observed
	 * by the graph for changes. Changes will then be reflected in edge presence
	 * and direction as appropriate, according to any rules which govern this.
	 *
	 * @param vertices
	 * @return
	 */
	public <W extends Expression<? extends V>> GraphConfigurator<W, E> verticesAsExpressions(
			Collection<W> vertices);

	/**
	 * This method wraps and forwards the parameters to
	 * {@link #verticesAsExpressions(Collection)}.
	 *
	 * @param vertices
	 * @return
	 */
	public default <W extends Expression<? extends V>> GraphConfigurator<W, E> verticesAsExpressions(
			@SuppressWarnings("unchecked") W... vertices) {
		return verticesAsExpressions(Arrays.asList(vertices));
	}

	/**
	 * Accepts a collection of vertex pairs for edges to be defined between.
	 *
	 * @param edges
	 * @return
	 */
	public GraphConfigurator<V, E> edges(Collection<EdgeVertices<V>> edges);

	/**
	 * This method wraps and forwards it's parameters to
	 * {@link #edges(Collection)}.
	 *
	 * @param edges
	 * @return
	 */
	public default GraphConfigurator<V, E> edges(
			@SuppressWarnings("unchecked") EdgeVertices<V>... edges) {
		return edges(Arrays.asList(edges));
	}

	/**
	 * This method wraps and forwards it's parameters to
	 * {@link #edges(Collection)}.
	 *
	 * @param edges
	 * @return
	 */
	public default GraphConfigurator<V, E> edges(Map<V, V> edges) {
		return edges(edges.entrySet().stream()
				.map(e -> new EdgeVertices<>(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
	}

	public GraphConfigurator<V, E> edgesBetween(
			Function<? super V, Collection<? extends V>> betweenNeighbours);

	public GraphConfigurator<V, E> edgesFrom(
			Function<? super V, Collection<? extends V>> fromNeighbours);

	public GraphConfigurator<V, E> edgesTo(
			Function<? super V, Collection<? extends V>> toNeighbours);

	public GraphConfigurator<V, E> edgeRule(
			BiPredicate<? super V, ? super V> betweenNeighbours);

	public GraphConfigurator<V, E> edgeRuleAsymmetric();

	public GraphConfigurator<V, E> directed();

	public GraphConfigurator<V, E> acyclic();

	public GraphConfigurator<V, E> multigraph();

	/**
	 * This method sets a comparator to determine the direction of an edge between
	 * two vertices. Vertices are passed to the comparator in the order they were
	 * given when defining the edge. The default behaviour, if no comparator is
	 * specified is as if a comparator has been specified which always returns 1.
	 * If the vertices of this graph are as expressions, then changes in the
	 * vertices will trigger reapplication of this comparator when needed.
	 *
	 * @param lowToHigh
	 * @return
	 */
	public default GraphConfigurator<V, E> direction(Comparator<V> lowToHigh) {
		return direction((e) -> lowToHigh);
	}

	/**
	 * This method accepts a function to create a comparator from an edge, over
	 * the vertices associated with that edge. The result of the application of
	 * this comparator will be used to determine the direction of that edge, with
	 * the semantics described by the {@link #direction(Comparator)} method. The
	 * function is only applied at most once per creation of an edge.
	 *
	 * @param lowToHigh
	 * @return
	 */
	public GraphConfigurator<V, E> direction(Function<E, Comparator<V>> lowToHigh);

	/**
	 * This method accepts a function over a pair of vertices resulting in an edge
	 * object. This function will be called every time an edge is added to the
	 * graph between a pair of vertices, with the result then being associated
	 * with that pair of vertices as an edge.
	 *
	 * @param factory
	 * @return
	 */
	public <F extends E> GraphConfigurator<V, F> edgeFactory(
			BiFunction<V, V, F> factory);

	/**
	 * This method behaves as {@link #edgeFactory(BiFunction)}, except that it can
	 * return a set, making the graph a multigraph.
	 *
	 * @param factory
	 * @return
	 */
	public <F extends E> GraphConfigurator<V, F> edgeMultiFactory(
			BiFunction<V, V, Set<F>> factory);

	/**
	 * This method behaves as {@link #edgeFactory(BiFunction)}, except that the
	 * creation of edge objects is independent of the associated vertex objects.
	 *
	 * @param factory
	 * @return
	 */
	public default <F extends E> GraphConfigurator<V, F> edgeFactory(
			Factory<F> factory) {
		return edgeFactory((v, w) -> factory.create());
	}

	/**
	 * This method accepts a metric over edge objects to describe their weight.
	 *
	 * @param factory
	 * @return
	 */
	public GraphConfigurator<V, E> edgeWeight(Function<E, Double> weight,
			boolean mutable);

	/**
	 * If this method is invoked, then for the graph created by this configurator
	 * any added edges, or partially satisfied triggered edge rules, will
	 * automatically trigger the addition of any missing vertices of that edge to
	 * the graph.
	 *
	 * @param factory
	 * @return
	 */
	public GraphConfigurator<V, E> generateNeighbours();

	public GraphConfigurator<V, E> constraint(Predicate<Graph<V, E>> constraint);

	public GraphConfigurator<V, E> comparator(Comparator<V> comparator);
}
