package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface XORable<S extends XORable<S, T> & Self<S>, T> {
	public S xor(T value);

	public S getXor(T value);
}
