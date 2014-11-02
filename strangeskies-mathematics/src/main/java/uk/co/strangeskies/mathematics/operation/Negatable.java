package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.utilities.Self;

public interface Negatable<S extends Negatable<S, N>, N extends Negatable<? extends N, ? extends S>>
		extends Self<S> {
	public N negate();

	public default N getNegated() {
		return copy().negate();
	}
}
