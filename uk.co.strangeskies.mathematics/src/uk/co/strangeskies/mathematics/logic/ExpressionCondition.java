package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.Expression;

public class ExpressionCondition implements Condition {
	private final Expression<? extends /**/BooleanValue> expression;

	public ExpressionCondition(
			Expression<? extends /**/BooleanValue> expression) {
		this.expression = expression;
	}

	@Override
	public final boolean isFulfilled() {
		return expression.getValue().getBooleanValue();
	}
}
