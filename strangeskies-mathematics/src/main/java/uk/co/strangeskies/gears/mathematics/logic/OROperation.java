package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class OROperation<O extends ORable<?, ? super T>, T> implements
		BiFunction<ORable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(ORable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getOr(secondOperand);
	}
}
