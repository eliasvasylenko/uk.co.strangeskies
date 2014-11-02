package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.utilities.Self;

public interface NonCommutativelyMultipliable<S extends NonCommutativelyMultipliable<S, T>, T>
		extends Multipliable<S, T>, Self<S> {
	public S preMultiply(T value);

	public default S getPreMultiplied(T value) {
		return copy().preMultiply(value);
	}
}
