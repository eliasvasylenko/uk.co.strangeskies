package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.utilities.Self;

public interface NOTable<S extends NOTable<S, N> & Self<S>, N extends NOTable<? extends N, ? extends S> & Self<N>> {
	public N not();

	public N getNot();
}
