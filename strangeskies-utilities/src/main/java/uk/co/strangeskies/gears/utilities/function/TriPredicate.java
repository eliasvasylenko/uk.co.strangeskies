package uk.co.strangeskies.gears.utilities.function;

public interface TriPredicate<O1, O2, O3> {
	public boolean test(O1 firstOperand, O2 secondOperand, O3 thirdOperand);
}
