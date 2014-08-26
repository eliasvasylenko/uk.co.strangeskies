package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.Function;

public class NOTOperation<O> implements Function<NOTable<?, ? extends O>, O> {
	@Override
	public O apply(NOTable<?, ? extends O> firstOperand) {
		return firstOperand.getNot();
	}
}
