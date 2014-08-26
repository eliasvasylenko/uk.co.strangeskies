package uk.co.strangeskies.gears.mathematics.graph;

import java.util.Comparator;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public interface GraphTransformer<V, E> extends Factory<Graph<V, E>> {
	public <W> GraphTransformer<W, E> vertices(Function<V, W> transformation);

	public <F> GraphTransformer<V, F> edges(Function<E, F> transformation);

	public GraphTransformer<V, E> edgeWeight(Function<E, Double> weight,
			boolean mutable);

	public GraphTransformer<V, E> direction(Comparator<V> lowToHigh);
}
