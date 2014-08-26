package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class SetTransformationFunction<F, T> extends
		CollectionTransformationFunction<F, T, Set<T>> {

	public SetTransformationFunction(Function<? super F, ? extends T> function) {
		super(function, new Factory<Set<T>>() {
			@Override
			public Set<T> create() {
				return new HashSet<>();
			}
		});
	}

	public static <X, Y> Set<Y> apply(Collection<? extends X> collection,
			Function<? super X, ? extends Y> function) {
		return new SetTransformationFunction<X, Y>(function).apply(collection);
	}
}
