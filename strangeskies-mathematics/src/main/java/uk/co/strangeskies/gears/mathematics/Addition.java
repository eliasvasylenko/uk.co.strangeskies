package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;

public class Addition<O extends Addable<?, ? super T>, T> extends
		BiFunctionExpression<Addable<? extends O, ? super T>, T, O> {
	public Addition(
			Expression<? extends Addable<? extends O, ? super T>> firstOperand,
			Expression<? extends T> secondOperand) {
		super(firstOperand, secondOperand, new Add<O, T>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " + " + getSecondOperand() + ")";
	}
}
