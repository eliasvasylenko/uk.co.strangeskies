package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.function.BiFunction;

import uk.co.strangeskies.gears.mathematics.values.Value;

public class PreRotate2<O> implements
		BiFunction<NonCommutativelyRotatable2<? extends O>, Value<?>, O> {
	@Override
	public O apply(NonCommutativelyRotatable2<? extends O> firstOperand,
			Value<?> secondOperand) {
		return firstOperand.getPreRotated(secondOperand);
	}
}
