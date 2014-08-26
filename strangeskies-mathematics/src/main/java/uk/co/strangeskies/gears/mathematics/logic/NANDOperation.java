package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class NANDOperation<O extends NANDable<?, ? super T>, T> implements
		BiFunction<NANDable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(NANDable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getNand(secondOperand);
	}
}
