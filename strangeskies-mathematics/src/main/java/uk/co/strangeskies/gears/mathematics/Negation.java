package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.expression.FunctionExpression;

public class Negation<O> extends
		FunctionExpression<Negatable<?, ? extends O>, O> {
	public Negation(Expression<? extends Negatable<?, ? extends O>> operand) {
		super(operand, new Negate<O>());
	}

	@Override
	public String toString() {
		return "¬" + getOperand();
	}
}
