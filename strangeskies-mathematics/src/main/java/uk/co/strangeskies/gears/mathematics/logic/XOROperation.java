package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class XOROperation<O extends XORable<?, ? super T>, T> implements
		BiFunction<XORable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(XORable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getXor(secondOperand);
	}
}
