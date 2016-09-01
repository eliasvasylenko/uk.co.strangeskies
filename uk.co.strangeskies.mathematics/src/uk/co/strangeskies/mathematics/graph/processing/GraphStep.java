package uk.co.strangeskies.mathematics.graph.processing;

public interface GraphStep<V, E> extends GraphWalker<V, E> {
	V previousLocation();

	E steppedEdge();
}
