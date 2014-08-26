package uk.co.strangeskies.gears.mathematics.logic;

import java.util.function.BiFunction;

public class XNOROperation<O extends XNORable<?, ? super T>, T> implements
		BiFunction<XNORable<? extends O, ? super T>, T, O> {
	@Override
	public O apply(XNORable<? extends O, ? super T> firstOperand, T secondOperand) {
		return firstOperand.getXnor(secondOperand);
	}
}
