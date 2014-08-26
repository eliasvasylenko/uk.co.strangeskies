package uk.co.strangeskies.gears.mathematics;

import java.util.function.BiFunction;

public class Multiply<O extends Multipliable<?, ? super T>, T>
		implements BiFunction<Multipliable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(Multipliable<? extends O, ? super T> firstOperand,
			T secondOperand) {
		return firstOperand.getMultiplied(secondOperand);
	}
}
