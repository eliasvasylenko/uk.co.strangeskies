package uk.co.strangeskies.gears.mathematics.logic;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.expression.FunctionExpression;

public class NOT<O> extends
		FunctionExpression<NOTable<?, ? extends O>, O> {
	public NOT(Expression<? extends NOTable<?, ? extends O>> operand) {
		super(operand, new NOTOperation<O>());
	}

	@Override
	public String toString() {
		return "¬" + getOperand();
	}
}
