package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.function.TriFunction;

public interface InterpolationFunction<T, I> extends
		TriFunction<T, T, Value<?>, I> {
}
