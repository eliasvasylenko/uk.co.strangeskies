package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class Multiplication<O extends Multipliable<?, ? super T>, T> extends
		BiFunctionExpression<Multipliable<? extends O, ? super T>, T, O> {
	public Multiplication(
			Expression<? extends Multipliable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		super(firstOperand, secondOperand, new Multiply<O, T>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
