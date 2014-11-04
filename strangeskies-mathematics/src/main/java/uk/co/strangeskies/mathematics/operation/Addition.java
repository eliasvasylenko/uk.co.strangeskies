package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class Addition<O extends Addable<?, ? super T>, T> extends
		BiFunctionExpression<Addable<? extends O, ? super T>, T, O> {
	public Addition(
			Expression<? extends Addable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getAdded(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " + " + getSecondOperand() + ")";
	}
}
