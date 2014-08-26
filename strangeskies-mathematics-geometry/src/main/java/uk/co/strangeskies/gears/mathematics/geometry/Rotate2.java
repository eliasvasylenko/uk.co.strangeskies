package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.function.BiFunction;

import uk.co.strangeskies.gears.mathematics.values.Value;

public class Rotate2<O> implements
		BiFunction<Rotatable2<? extends O>, Value<?>, O> {
	@Override
	public O apply(Rotatable2<? extends O> firstOperand, Value<?> secondOperand) {
		return firstOperand.getRotated(secondOperand);
	}
}
