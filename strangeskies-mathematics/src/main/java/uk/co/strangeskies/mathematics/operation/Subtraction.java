package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class Subtraction<O extends Subtractable<?, ? super T>, T> extends
		BiFunctionExpression<Subtractable<? extends O, ? super T>, T, O> {
	public Subtraction(
			Expression<? extends Subtractable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		super(firstOperand, secondOperand, Subtractable::subtract);
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " - " + getSecondOperand() + ")";
	}
}
