package uk.co.strangeskies.gears.utilities.function.collection;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.Factory;

public class CollectionTransformationFunction<F, T, C extends Collection<T>>
		implements Function<Collection<? extends F>, C> {
	private final Function<? super F, ? extends T> function;
	private final Factory<C> collectionFactory;

	public CollectionTransformationFunction(
			Function<? super F, ? extends T> function, Factory<C> collectionFactory) {
		this.function = function;
		this.collectionFactory = collectionFactory;
	}

	@Override
	public final C apply(Collection<? extends F> input) {
		C collection = collectionFactory.create();
		for (F item : input) {
			collection.add(function.apply(item));
		}
		return collection;
	}

	public final Function<? super F, ? extends T> getFunction() {
		return function;
	}
}
