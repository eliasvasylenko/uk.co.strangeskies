package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.values.Value;

public class Scaling<O> extends
		BiFunctionExpression<Scalable<? extends O>, Value<?>, O> {
	public Scaling(Expression<? extends Scalable<? extends O>> firstOperand,
			Expression<? extends Value<?>> secondOperand) {
		super(firstOperand, secondOperand, new Scale<O>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
