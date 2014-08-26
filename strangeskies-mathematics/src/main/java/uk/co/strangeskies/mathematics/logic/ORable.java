package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface ORable<S extends ORable<S, T> & Self<S>, T> {
	public S or(T value);

	public S getOr(T value);
}
