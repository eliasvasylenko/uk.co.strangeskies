package uk.co.strangeskies.mathematics.graph.processing;

import java.util.List;
import java.util.function.BiFunction;

import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.processing.GraphWalker.Marker;

public interface Pathfinder {
	public interface Solver<V, E> {
		List<V> verticesBetween(V from, V to);

		List<E> edgesBetween(V from, V to);
	}

	<V, E> Solver<V, E> over(Graph<V, E> graph);

	<V, E> Marker<V, E> over(Graph<V, E> graph,
			BiFunction<? super V, ? super V, ? extends Double> heuristic);
}
