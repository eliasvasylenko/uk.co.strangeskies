package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class ANDOperation<O extends ANDable<?, ? super T>, T> implements
		BiFunction<ANDable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(ANDable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getAnd(secondOperand);
	}
}
