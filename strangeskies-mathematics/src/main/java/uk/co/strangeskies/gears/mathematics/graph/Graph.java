package uk.co.strangeskies.gears.mathematics.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.Copyable;

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

	public default E removeEdge(V from, V to) {
		throw new UnsupportedOperationException();
	}

	public default E addEdge(V from, V to, boolean addMissingVertices) {
		if (addMissingVertices) {
			addVertex(from);
			addVertex(to);
		} else if (!getVertices().contains(from) || !getVertices().contains(to))
			return null;
		return addEdge(from, to);
	}

	public default boolean addEdges(Collection<? extends V> from, V to) {
		return addEdges(from, to, false);
	}

	public default boolean addEdges(V from, Collection<? extends V> to) {
		return addEdges(from, to, false);
	}

	public default boolean addEdges(Collection<? extends V> from, V to,
			boolean addMissingVertices) {
		boolean changed = false;
		for (V f : from) {
			changed = addEdge(f, to, addMissingVertices) != null | changed;
		}
		return changed;
	}

	public default boolean addEdges(V from, Collection<? extends V> to,
			boolean addMissingVertices) {
		boolean changed = false;
		for (V t : to) {
			changed = addEdge(from, t, addMissingVertices) != null | changed;
		}
		return changed;
	}

	public default boolean removeEdge(E edge) {
		EdgeVertices<V> vertices = getVertices(edge);
		return removeEdge(vertices.getFrom(), vertices.getTo()) != null;
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
