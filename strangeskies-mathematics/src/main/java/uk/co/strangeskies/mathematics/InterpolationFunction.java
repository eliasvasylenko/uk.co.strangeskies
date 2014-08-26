package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.function.TriFunction;

public interface InterpolationFunction<T, I> extends
		TriFunction<T, T, Value<?>, I> {
}
