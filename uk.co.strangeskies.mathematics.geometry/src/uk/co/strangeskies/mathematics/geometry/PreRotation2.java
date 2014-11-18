package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.values.Value;

public class PreRotation2<O> extends
		BiFunctionExpression<NonCommutativelyRotatable2<? extends O>, Value<?>, O> {
	public PreRotation2(
			Expression<? extends NonCommutativelyRotatable2<? extends O>> firstOperand,
			Expression<? extends Value<?>> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getPreRotated(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
