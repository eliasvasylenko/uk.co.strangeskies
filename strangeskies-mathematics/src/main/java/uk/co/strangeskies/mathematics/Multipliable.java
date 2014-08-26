package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.utilities.Self;

public interface Multipliable<S extends Multipliable<S, T>, T> extends Self<S> {
	public S multiply(T value);

	public S getMultiplied(T value);
}
