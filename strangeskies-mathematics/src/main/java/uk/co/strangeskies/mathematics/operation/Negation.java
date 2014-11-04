package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.FunctionExpression;

public class Negation<O> extends
		FunctionExpression<Negatable<?, ? extends O>, O> {
	public Negation(Expression<? extends Negatable<?, ? extends O>> operand) {
		super(operand, n -> n.getNegated());
	}

	@Override
	public String toString() {
		return "¬" + getOperand();
	}
}
