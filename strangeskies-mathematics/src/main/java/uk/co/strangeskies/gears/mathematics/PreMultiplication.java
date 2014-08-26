package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;

public class PreMultiplication<O extends NonCommutativelyMultipliable<?, ? super T>, T>
		extends
		BiFunctionExpression<NonCommutativelyMultipliable<? extends O, ? super T>, T, O> {
	public PreMultiplication(
			Expression<? extends NonCommutativelyMultipliable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		super(firstOperand, secondOperand, new PreMultiply<O, T>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
