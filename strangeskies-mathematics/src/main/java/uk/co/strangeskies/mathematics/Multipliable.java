package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.utilities.Self;

public interface Multipliable<S extends Multipliable<S, T>, T> extends Self<S> {
	public S multiply(T value);

	public default S getMultiplied(T value) {
		return copy().multiply(value);
	}
}
