package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.utilities.Self;

public interface NORable<S extends NORable<S, T> & Self<S>, T> {
	public S nor(T value);

	public S getNor(T value);
}
