package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class PreMultiplication<O extends NonCommutativelyMultipliable<?, ? super T>, T>
		extends
		BiFunctionExpression<NonCommutativelyMultipliable<? extends O, ? super T>, T, O> {
	public PreMultiplication(
			Expression<? extends NonCommutativelyMultipliable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		/*
		 * TODO GETTING SICK OF THESE JAVAC BUGS. Yet another place I can't use ::
		 * syntax because Oracle javac is broken.
		 */
		super(firstOperand, secondOperand, (a, b) -> a.getPreMultiplied(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
