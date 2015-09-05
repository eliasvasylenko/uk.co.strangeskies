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
package uk.co.strangeskies.mathematics.graph.building;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.Graph.Edges;
import uk.co.strangeskies.utilities.factory.Factory;

/**
 *
 * @author Elias N Vasylenko
 * @param <V>
 * @param <E>
 */
public interface GraphConfigurator<V, E> extends Factory<Graph<V, E>> {
	/**
	 * Calling this method has the same effect as calling both
	 * {@link #readOnlyVertices()} and {@link #readOnlyEdges()}.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> readOnly();

	/**
	 * Calling this method has the effect of making the resulting graph
	 * unmodifiable through the manual addition of vertices.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> readOnlyVertices();

	/**
	 * Calling this method has the effect of making the resulting graph
	 * unmodifiable through the manual addition of edges.
	 *
	 * @return
	 */
	public GraphConfigurator<V, E> readOnlyEdges();

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
	 * Accepts a collection of vertex pairs for edges to be defined between.
	 *
	 * @param edges
	 * @return
	 */
	public GraphConfigurator<V, E> edges(
			Collection<? extends EdgeVertices<V>> edges);

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
	public default GraphConfigurator<V, E> edge(V from, V to) {
		return edges(EdgeVertices.between(from, to));
	}

	public GraphConfigurator<V, E> edges(Map<E, EdgeVertices<V>> edges);

	public GraphConfigurator<V, E> addEdge(E edge, V from, V to);

	/**
	 * The provided mapping, from a vertex to a set of vertices, will be applied
	 * to each new vertex which is added to the graph. Edges will then be
	 * generated between the new vertex and each vertex in this set.
	 * 
	 * <p>
	 * If the an edge is generated to a vertex which is not in the graph, the edge
	 * may either be ignored, or the missing vertex may be added to the graph
	 * before the edge is generated, thus potentially triggering further edge
	 * generation for the newly generated vertex in a cascading fashion.
	 * 
	 * <p>
	 * Note that the mapping will not be reapplied to vertices already in the
	 * graph when new vertices are added, so unless mapped vertices are generated
	 * as described above, the mapping should be bidirectional in most use-cases.
	 * In cases where we need to check a new vertex against each existing vertex
	 * individually, {@link #edgeGenerationRule(BiPredicate)} should be used.
	 * 
	 * <p>
	 * If the graph is directed, edges will be generated in both directions. To
	 * generate edges in only one direction,
	 * {@link #generateEdgesFrom(Function, boolean)} or
	 * {@link #generateEdgesTo(Function, boolean)} should be used.
	 *
	 * @param betweenNeighbours
	 *          A function describing a mapping from a vertex to a set of
	 *          vertices, such that an edge may be generated between that vertex
	 *          and each vertex in the mapped set
	 * @param generateVertices
	 *          If true, mapped vertices which are not a member of the graph will
	 *          be added to the graph before an edge is created, otherwise an edge
	 *          will not be created to that vertex
	 * @return The receiving configurator
	 */
	public GraphConfigurator<V, E> generateEdgesBetween(
			Function<? super V, ? extends Collection<? extends V>> betweenNeighbours,
			boolean generateVertices);

	/**
	 * The provided mapping, from a vertex to a set of vertices, will be applied
	 * to each new vertex which is added to the graph. Edges will then be
	 * generated <em>to</em> the new vertex <em>from</em> each vertex in this set.
	 * The graph will be directional.
	 * 
	 * <p>
	 * If the an edge is generated from a vertex which is not in the graph, the
	 * edge may either be ignored, or the missing vertex may be added to the graph
	 * before the edge is generated, thus potentially triggering further edge
	 * generation for the newly generated vertex in a cascading fashion.
	 * 
	 * <p>
	 * Note that the mapping will not be reapplied to vertices already in the
	 * graph when new vertices are added, so unless mapped vertices are generated
	 * as described above, the mapping should form a bidirectional relationship
	 * with a mapping provided to {@link #generateEdgesTo(Function, boolean)} in
	 * most use-cases. In cases where we need to check a new vertex against each
	 * existing vertex individually, {@link #edgeGenerationRule(BiPredicate)}
	 * should be used.
	 *
	 * @param fromNeighbours
	 *          A function describing a mapping from a vertex to a set of
	 *          vertices, such that an edge may be generated from that vertex to
	 *          each vertex in the mapped set
	 * @param generateVertices
	 *          If true, mapped vertices which are not a member of the graph will
	 *          be added to the graph before an edge is created, otherwise an edge
	 *          will not be created to that vertex
	 * @return The receiving configurator
	 */
	public GraphConfigurator<V, E> generateEdgesFrom(
			Function<? super V, ? extends Collection<? extends V>> fromNeighbours,
			boolean generateVertices);

	/**
	 * The provided mapping, from a vertex to a set of vertices, will be applied
	 * to each new vertex which is added to the graph. Edges will then be
	 * generated <em>from</em> the new vertex <em>to</em> each vertex in this set.
	 * The graph will be directional.
	 * 
	 * <p>
	 * If the an edge is generated to a vertex which is not in the graph, the edge
	 * may either be ignored, or the missing vertex may be added to the graph
	 * before the edge is generated, thus potentially triggering further edge
	 * generation for the newly generated vertex in a cascading fashion.
	 * 
	 * <p>
	 * Note that the mapping will not be reapplied to vertices already in the
	 * graph when new vertices are added, so unless mapped vertices are generated
	 * as described above, the mapping should form a bidirectional relationship
	 * with a mapping provided to {@link #generateEdgesTo(Function, boolean)} in
	 * most use-cases. In cases where we need to check a new vertex against each
	 * existing vertex individually, {@link #edgeGenerationRule(BiPredicate)}
	 * should be used.
	 *
	 * @param toNeighbours
	 *          A function describing a mapping from a vertex to a set of
	 *          vertices, such that an edge may be generated from that vertex to
	 *          each vertex in the mapped set
	 * @param generateVertices
	 *          If true, mapped vertices which are not a member of the graph will
	 *          be added to the graph before an edge is created, otherwise an edge
	 *          will not be created to that vertex
	 * @return The receiving configurator
	 */
	public GraphConfigurator<V, E> generateEdgesTo(
			Function<? super V, ? extends Collection<? extends V>> toNeighbours,
			boolean generateVertices);

	/**
	 * Provide a rule to determine whether edges can be legally created between
	 * two vertices. If an attempt to add an edge which is illegal by this
	 * standard, the edge will fail to add, and an exception will optionally be
	 * thrown.
	 * 
	 * <p>
	 * If the graph is directed, the vertex the edge comes from and the vertex it
	 * goes to are passed as the first and second arguments respectively.
	 * Otherwise, the operation defined by the given predicate is assumed to be
	 * commutative.
	 * 
	 * @param validateBetweenVertices
	 * @param throwOnFailure
	 *          True if an exception should be thrown if an attempt to add an edge
	 *          fails to validate, false otherwise.
	 * @return The receiving configurator
	 */
	public GraphConfigurator<V, E> edgeValidationRule(
			BiPredicate<? super V, ? super V> validateBetweenVertices,
			boolean throwOnFailure);

	/**
	 * Provide a condition over two vertices such that upon fulfilment an edge is
	 * to be automatically generated between those vertices.
	 * 
	 * <p>
	 * If the graph is directed, the vertex the edge comes from and the vertex it
	 * goes to are passed as the first and second arguments respectively.
	 * Otherwise, the operation defined by the given predicate is assumed to be
	 * commutative.
	 * 
	 * <p>
	 * Be aware that this behaviour is relatively computationally expensive, as
	 * every possible vertex pair must be checked. Adding vertices will be
	 * quadratic with the number of vertices added, or if only one vertex is
	 * added, linear with the current number of vertices.
	 *
	 * @param generateBetweenVertices
	 * @return The receiving configurator
	 */
	public GraphConfigurator<V, E> edgeGenerationRule(
			BiPredicate<? super V, ? super V> generateBetweenVertices);

	public default GraphConfigurator<V, E> directed() {
		return direction((a, b) -> 1);
	}

	public GraphConfigurator<V, E> acyclic();

	public GraphConfigurator<V, E> multigraph();

	/**
	 * This method sets a comparator to determine the direction of an edge between
	 * two vertices. Vertices are passed to the comparator in the order they were
	 * given when defining the edge. The default behaviour, if no comparator is
	 * specified is as if a comparator has been specified which always returns 1.
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
	 * the semantics described by the {@link #direction(Comparator)} method.
	 * 
	 * The function is only applied at most once per creation of an edge.
	 *
	 * @param lowToHigh
	 * @return
	 */
	public GraphConfigurator<V, E> direction(Function<E, Comparator<V>> lowToHigh);

	/**
	 * For simple <em>or</em> multigraphs, specify the mechanism by which edge
	 * objects will be generated between two vertices when an edge is added
	 * between them.
	 * 
	 * <p>
	 * The provided function will be invoked when an edge is added between two
	 * vertices without an edge object being explicitly provided.
	 * 
	 * <p>
	 * If no edge factory is provided, by way of either this method or
	 * {@link #edgeMultiFactory(Function)}, edge objects must be explicitly
	 * provided when adding an edge between vertices, by way of e.g.
	 * {@link Edges#put(Object, Object)} or {@link #edges(Map)}. Conversely, if an
	 * edge factory <em>is</em> provided, it will not be possible to add edges in
	 * that manner.
	 */
	public <F extends E> GraphConfigurator<V, F> edgeFactory(
			Function<EdgeVertices<V>, F> factory);

	/**
	 * For multigraphs, specify the mechanism by which edge objects will be
	 * generated between two vertices when an edge is added between them.
	 * 
	 * <p>
	 * The provided function will be invoked when an edge is added between two
	 * vertices without an edge object being explicitly provided.
	 * 
	 * <p>
	 * If no edge factory is provided, by way of either this method or
	 * {@link #edgeMultiFactory(Function)}, edge objects must be explicitly
	 * provided when adding an edge between vertices, by way of e.g.
	 * {@link Edges#put(Object, Object)} or {@link #edges(Map)}. Conversely, if an
	 * edge factory <em>is</em> provided, it will not be possible to add edges in
	 * that manner.
	 */
	public <F extends E> GraphConfigurator<V, F> edgeMultiFactory(
			Function<EdgeVertices<V>, Set<F>> factory);

	/**
	 * This method behaves as {@link #edgeFactory(Function)}, except that the
	 * creation of edge objects is independent of the associated vertex objects.
	 *
	 * @param factory
	 * @return
	 */
	public default <F extends E> GraphConfigurator<V, F> edgeFactory(
			Factory<F> factory) {
		return edgeFactory(v -> factory.create());
	}

	/**
	 * This method accepts a mapping from an edge to a weight for that edge. If no
	 * mapping is provided, the graph will be considered unweighted.
	 *
	 * @param weight
	 * @return
	 */
	public GraphConfigurator<V, E> edgeWeight(Function<E, Double> weight);

	public GraphConfigurator<V, E> constrain(Predicate<Graph<V, E>> constraint);

	public GraphConfigurator<V, E> vertexComparator(
			Comparator<? super V> comparator);

	public GraphConfigurator<V, E> edgeComparator(Comparator<? super E> comparator);
}
