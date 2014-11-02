package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.utilities.Self;

public interface Incrementable<S extends Incrementable<S>> extends Self<S> {
	public S increment();

	public S decrement();

	public default S getIncremented() {
		return copy().increment();
	}

	public default S getDecremented() {
		return copy().decrement();
	}
}
