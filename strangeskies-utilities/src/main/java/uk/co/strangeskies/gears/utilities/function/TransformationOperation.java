package uk.co.strangeskies.gears.utilities.function;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TransformationOperation<T, F> implements BiFunction<T, F, T> {
	private final Function<? super F, ? extends T> function;

	public TransformationOperation(Function<? super F, ? extends T> function) {
		this.function = function;
	}

	@Override
	public T apply(T assignee, F assignment) {
		return function.apply(assignment);
	}
}
