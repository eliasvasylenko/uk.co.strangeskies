package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.values.Value;

public class Rotation2<O> extends
		BiFunctionExpression<Rotatable2<? extends O>, Value<?>, O> {
	public Rotation2(Expression<? extends Rotatable2<? extends O>> firstOperand,
			Expression<? extends Value<?>> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getRotated(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
