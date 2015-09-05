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
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.building.GraphConfigurator;
import uk.co.strangeskies.utilities.factory.Configurator;

public class GraphConfiguratorImpl<V, E> extends Configurator<Graph<V, E>>
		implements GraphConfigurator<V, E> {
	private Collection<V> vertices;
	private boolean unmodifiableVertices;
	private Comparator<? super V> vertexComparator;

	private Collection<EdgeVertices<V>> edgeVertices;
	private Collection<? extends E> edges;
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
	private BiPredicate<? super V, ? super V> edgeGenerationRule;

	private BiPredicate<? super V, ? super V> edgeValidationRule;

	private Predicate<Graph<V, E>> constraint;

	protected static GraphConfigurator<Object, Object> configure() {
		return new GraphConfiguratorImpl<>();
	}

	@Override
	public Graph<V, E> tryCreate() {
		return new GraphImpl<V, E>(this);
	}

	@Override
	public GraphConfigurator<V, E> readOnly() {
		return readOnlyVertices().readOnlyEdges();
	}

	@Override
	public GraphConfigurator<V, E> readOnlyVertices() {
		unmodifiableVertices = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> readOnlyEdges() {
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
	public GraphConfigurator<V, E> edges(
			Collection<? extends EdgeVertices<V>> edges) {
		assertConfigurable(edgeVertices);
		assertConfigurable(this.edges);
		assertConfigurable(edgeMap);

		this.edgeVertices = (Collection<EdgeVertices<V>>) edges;
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
		assertConfigurable(getEdgeWeight());
		edgeWeight = weight;

		return this;
	}

	@Override
	public GraphConfigurator<V, E> vertexComparator(
			Comparator<? super V> comparator) {
		assertConfigurable(vertexComparator);
		this.vertexComparator = comparator;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> edgeComparator(Comparator<? super E> comparator) {
		assertConfigurable(this.edgeComparator);
		this.edgeComparator = comparator;
		return this;
	}

	boolean isDirected() {
		return directed;
	}

	boolean isMultigraph() {
		return multigraph;
	}

	Function<E, Double> getEdgeWeight() {
		return edgeWeight;
	}

	Comparator<? super V> getVertexComparator() {
		return vertexComparator;
	}

	Comparator<? super E> getEdgeComparator() {
		return edgeComparator;
	}

	Function<EdgeVertices<V>, E> getEdgeFactory() {
		return edgeFactory;
	}

	Function<? super V, ? extends Collection<? extends V>> getEdgeGenerator() {
		return edgeGenerator;
	}

	Function<? super V, ? extends Collection<? extends V>> getOutgoingEdgeGenerator() {
		return outgoingEdgeGenerator;
	}

	Function<? super V, ? extends Collection<? extends V>> getIncomingEdgeGenerator() {
		return incomingEdgeGenerator;
	}

	BiPredicate<? super V, ? super V> getEdgeRule() {
		return edgeGenerationRule;
	}

	Collection<V> getVertices() {
		return vertices;
	}

	Collection<EdgeVertices<V>> getEdgeVertices() {
		return edgeVertices;
	}

	@Override
	public GraphConfigurator<V, E> addEdge(E edge, V from, V to) {
		// TODO Auto-generated method stub
		return null;
	}
}
