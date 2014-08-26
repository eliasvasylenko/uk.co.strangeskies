package uk.co.strangeskies.gears.utilities.function;

public interface TriFunction<O1, O2, O3, R> {
	public R apply(O1 firstOperand, O2 secondOperand, O3 thirdOperand);
}
