package uk.co.strangeskies.mathematics.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.Copyable;

public interface Graph<V, E> extends Copyable<Graph<V, E>> {
	public Set<V> getVertices();

	public Set<E> getEdges();

	public default Set<EdgeVertices<V>> getEdgeVertices() {
		return getEdges().stream().map(e -> getVertices(e))
				.collect(Collectors.toSet());
	}

	public E getEdge(V from, V to);

	public default Set<E> getEdges(V from, V to) {
		Set<E> edges = new HashSet<>();
		edges.add(getEdge(from, to));
		return edges;
	}

	public EdgeVertices<V> getVertices(E edge);

	public Set<V> getAdjacentVertices(V vertex);

	public Set<E> getAdjacentEdges(V vertex);

	public Set<E> getOutgoingEdges(V vertex);

	public Set<E> getIncomingEdges(V vertex);

	public default V getFrom(E edge) {
		return getVertices(edge).getFrom();
	}

	public default V getTo(E edge) {
		return getVertices(edge).getTo();
	}

	public default boolean addVertex(V vertex) {
		throw new UnsupportedOperationException();
	}

	public default boolean addVertices(Collection<? extends V> vertices) {
		boolean modified = false;
		for (V vertex : vertices) {
			modified = addVertex(vertex) || modified;
		}
		return modified;
	}

	public default boolean removeVertex(V vertex) {
		throw new UnsupportedOperationException();
	}

	public default E addEdge(V from, V to) {
		throw new UnsupportedOperationException();
	}

	public default E addEdge(EdgeVertices<V> edge) {
		throw new UnsupportedOperationException();
	}

	public default boolean addEdges(Set<EdgeVertices<V>> edgeVertices) {
		boolean changed = false;
		for (EdgeVertices<V> edge : edgeVertices) {
			changed = addEdge(edge) != null | changed;
		}
		return changed;
	}

	public default E removeEdge(V from, V to) {
		throw new UnsupportedOperationException();
	}

	public default Set<E> removeEdges(V from, V to) {
		throw new UnsupportedOperationException();
	}

	public default boolean removeEdge(E edge) {
		throw new UnsupportedOperationException();
	}

	public boolean isDirected();

	public boolean isWeighted();

	public boolean isSimple();

	public double weight(E edge);

	public default double weight(V from, V to) {
		return weight(getEdge(from, to));
	}

	public GraphTransformer<V, E> transform();
}
