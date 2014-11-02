package uk.co.strangeskies.mathematics.graph.processing;

import java.util.Set;

import uk.co.strangeskies.mathematics.graph.Graph;

public interface GraphWalker {
	public interface Marker<V, E> {
		V currentLocation();

		default boolean isTerminated() {
			return availableVertices().isEmpty();
		}

		Set<V> availableVertices();

		Set<E> availableEdges();

		E moveTo(V nextVertex);

		V moveThrough(E nextEdge);
	}

	<V, E> Marker<V, E> atEntryPoint(Graph<V, E> graph, V entryPoint);

	<V, E> Marker<V, E> atRoot(Graph<V, E> graph);
}
