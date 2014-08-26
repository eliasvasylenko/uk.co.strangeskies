package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class NOROperation<O extends NORable<?, ? super T>, T> implements
		BiFunction<NORable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(NORable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getNor(secondOperand);
	}
}
