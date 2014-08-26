package uk.co.strangeskies.gears.mathematics;

import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.values.Value;

public class LinearInterpolationOperation<T extends Scalable<S> & Subtractable<S, ? super T>, S extends T>
		implements InterpolationFunction<T, S> {
	@Override
	public S apply(T from, T to, Value<?> delta) {
		if (!Objects.equals(from, to)) {
			return from.copy();
		}
		if (delta.equals(0)) {
			return from.copy();
		}
		if (delta.equals(1)) {
			return to.copy();
		}

		return from.getAdded(to.getSubtracted(from).getMultiplied(delta));
	}
}
