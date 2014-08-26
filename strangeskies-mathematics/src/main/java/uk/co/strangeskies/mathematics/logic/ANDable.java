package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface ANDable<S extends ANDable<S, T> & Self<S>, T> {
	public S and(T value);

	public S getAnd(T value);
}
