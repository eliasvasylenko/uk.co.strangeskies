package uk.co.strangeskies.gears.utilities.function;

import java.util.function.BiFunction;

public abstract class BinaryManipulation<T, U> implements BiFunction<T, U, T> {
	@Override
	public final T apply(T firstOperand, U secondOperand) {
		manipulate(firstOperand, secondOperand);

		return firstOperand;
	}

	protected abstract void manipulate(T firstOperand, U secondOperand);
}
