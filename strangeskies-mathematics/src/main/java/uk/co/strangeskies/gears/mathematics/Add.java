package uk.co.strangeskies.gears.mathematics;

import java.util.function.BiFunction;

public class Add<O extends Addable<?, ? super T>, T> implements
		BiFunction<Addable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(Addable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getAdded(secondOperand);
	}
}
