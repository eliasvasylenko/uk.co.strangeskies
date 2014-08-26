package uk.co.strangeskies.gears.mathematics;

import java.util.function.BiFunction;

public class Subtract<O extends Subtractable<?, ? super T>, T> implements
		BiFunction<Subtractable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(Subtractable<? extends O, ? super T> firstOperand,
			T secondOperand) {
		return firstOperand.getSubtracted(secondOperand);
	}
}
