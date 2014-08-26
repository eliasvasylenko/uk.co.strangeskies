package uk.co.strangeskies.gears.mathematics.graph.impl;

import java.util.Comparator;
import java.util.function.Function;

import uk.co.strangeskies.gears.mathematics.graph.Graph;
import uk.co.strangeskies.gears.mathematics.graph.GraphTransformer;

public class GraphTransformerImpl<V, E> implements GraphTransformer<V, E> {
	private final Graph<V, E> source;

	public GraphTransformerImpl(Graph<V, E> source) {
		this.source = source;
	}

	@Override
	public Graph<V, E> create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <W> GraphTransformer<W, E> vertices(Function<V, W> transformation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F> GraphTransformer<V, F> edges(Function<E, F> transformation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphTransformer<V, E> edgeWeight(Function<E, Double> weight,
			boolean mutable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphTransformer<V, E> direction(Comparator<V> lowToHigh) {
		// TODO Auto-generated method stub
		return null;
	}
}
