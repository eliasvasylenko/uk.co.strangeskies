package uk.co.strangeskies.mathematics.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.utilities.Copyable;

public interface Graph<V, E> extends Copyable<Graph<V, E>> {
	public interface Vertices<V> extends Set<V> {
		Set<V> adjacentTo(V vertex);

		Set<V> outgoingFrom(V vertex);

		Set<V> incomingTo(V vertex);

		Comparator<V> comparator();
	}

	public interface Edges<E, V> extends Map<E, EdgeVertices<V>> {
		Set<E> adjacentTo(V vertex);

		Set<E> outgoingFrom(V vertex);

		Set<E> incomingTo(V vertex);

		Set<E> between(V from, V to);

		default Set<E> between(EdgeVertices<V> vertices) {
			return between(vertices.getFrom(), vertices.getTo());
		}

		/**
		 * If there is exactly one edge between the provided vertices, this edge is
		 * returned, otherwise null is returned.
		 *
		 * @param from
		 * @param to
		 * @return
		 */
		E betweenUnique(V from, V to);

		default E betweenUnique(EdgeVertices<V> vertices) {
			return betweenUnique(vertices.getFrom(), vertices.getTo());
		}

		E add(V from, V to);

		default E add(EdgeVertices<V> edge) {
			return add(edge.getFrom(), edge.getTo());
		}

		default boolean add(@SuppressWarnings("unchecked") EdgeVertices<V>... edges) {
			return add(Arrays.asList(edges));
		}

		default boolean add(Collection<? extends EdgeVertices<V>> edgeVertices) {
			boolean changed = false;
			for (EdgeVertices<V> edge : edgeVertices) {
				changed = add(edge) != null | changed;
			}
			return changed;
		}

		double weight(E edge);
	}

	/**
	 * Returns an unmodifiable set of the vertices in this graph.
	 *
	 * @return
	 */
	Vertices<V> vertices();

	/**
	 * Returns an unmodifiable set of the edges in this graph.
	 *
	 * @return
	 */
	Edges<E, V> edges();

	boolean isDirected();

	boolean isWeighted();

	boolean isSimple();

	GraphTransformer<V, E> transform();
}
