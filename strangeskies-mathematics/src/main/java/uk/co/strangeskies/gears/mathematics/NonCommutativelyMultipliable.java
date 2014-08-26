package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.utilities.Self;

public interface NonCommutativelyMultipliable<S extends NonCommutativelyMultipliable<S, T>, T>
		extends Multipliable<S, T>, Self<S> {
	public S preMultiply(T value);

	public S getPreMultiplied(T value);
}
