package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.utilities.Self;

public interface ORable<S extends ORable<S, T> & Self<S>, T> {
	public S or(T value);

	public S getOr(T value);
}
