package uk.co.strangeskies.gears.mathematics;

import java.util.function.BiFunction;

import uk.co.strangeskies.gears.mathematics.values.Value;

public class Scale<O> implements BiFunction<Scalable<? extends O>, Value<?>, O> {
	@Override
	public O apply(Scalable<? extends O> firstOperand, Value<?> secondOperand) {
		return firstOperand.getMultiplied(secondOperand);
	}
}
