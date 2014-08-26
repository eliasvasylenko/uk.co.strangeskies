package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.values.Value;

public class Rotation2<O> extends
		BiFunctionExpression<Rotatable2<? extends O>, Value<?>, O> {
	public Rotation2(Expression<? extends Rotatable2<? extends O>> firstOperand,
			Expression<? extends Value<?>> secondOperand) {
		super(firstOperand, secondOperand, new Rotate2<O>());
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
