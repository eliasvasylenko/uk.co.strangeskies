package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class ListTransformationFunction<F, T> extends
		CollectionTransformationFunction<F, T, List<T>> {

	public ListTransformationFunction(Function<? super F, ? extends T> function) {
		super(function, new Factory<List<T>>() {
			@Override
			public List<T> create() {
				return new ArrayList<>();
			}
		});
	}

	public static <X, Y> List<Y> apply(Collection<? extends X> collection,
			Function<? super X, ? extends Y> function) {
		return new ListTransformationFunction<X, Y>(function).apply(collection);
	}

	@SuppressWarnings("unchecked")
	public static <X, Y> Y[] apply(X[] collection,
			Function<? super X, ? extends Y> function) {
		return (Y[]) new ListTransformationFunction<X, Y>(function).apply(
				Arrays.asList(collection)).toArray();
	}
}