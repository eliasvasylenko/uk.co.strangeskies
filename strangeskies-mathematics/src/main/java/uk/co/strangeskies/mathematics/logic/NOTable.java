package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.utilities.Self;

public interface NOTable<S extends NOTable<S, N> & Self<S>, N extends NOTable<? extends N, ? extends S> & Self<N>> {
	public N not();

	public N getNot();
}
