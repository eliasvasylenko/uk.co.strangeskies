package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface NANDable<S extends NANDable<S, T> & Self<S>, T> {
	public S nand(T value);

	public S getNand(T value);
}
