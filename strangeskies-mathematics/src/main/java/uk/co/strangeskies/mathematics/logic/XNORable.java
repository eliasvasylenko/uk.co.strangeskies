package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface XNORable<S extends XNORable<S, T> & Self<S>, T> {
	public S xnor(T value);

	public S getXnor(T value);
}
