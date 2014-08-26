package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.utilities.Self;

public interface NonCommutativelyMultipliable<S extends NonCommutativelyMultipliable<S, T>, T>
		extends Multipliable<S, T>, Self<S> {
	public S preMultiply(T value);

	public S getPreMultiplied(T value);
}
