package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface NORable<S extends NORable<S, T> & Self<S>, T> {
	public S nor(T value);

	public S getNor(T value);
}
