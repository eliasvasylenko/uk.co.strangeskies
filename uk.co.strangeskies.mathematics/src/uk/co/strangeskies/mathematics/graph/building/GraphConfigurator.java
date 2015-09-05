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
import java.util.function.Function;

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
	 * The graph will be directed, and the direction of an edge will be determined
	 * by the order in which the vertices for that edge are given when an edge is
	 * added.
	 * 
	 * @return
	 */
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

	public GraphConfigurator<V, E> vertexComparator(
			Comparator<? super V> comparator);

	public GraphConfigurator<V, E> edgeComparator(Comparator<? super E> comparator);
}
