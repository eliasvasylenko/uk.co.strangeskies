package uk.co.strangeskies.gears.mathematics;

import java.util.function.BiFunction;

public class PreMultiply<O extends NonCommutativelyMultipliable<?, ? super T>, T>
		implements
		BiFunction<NonCommutativelyMultipliable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(
			NonCommutativelyMultipliable<? extends O, ? super T> firstOperand,
			T secondOperand) {
		return firstOperand.getPreMultiplied(secondOperand);
	}
}
