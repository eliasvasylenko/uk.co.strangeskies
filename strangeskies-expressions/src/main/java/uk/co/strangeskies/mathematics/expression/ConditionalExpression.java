package uk.co.strangeskies.mathematics.expression;

public class ConditionalExpression<O> extends
		FunctionExpression</* @ReadOnly */Boolean, O> {

	public ConditionalExpression(
			Expression<? extends /* @ReadOnly */Boolean> condition,
			final O valueWhenFulfilled, final O valueWhenUnfulfilled) {
		super(condition, t -> t ? valueWhenFulfilled : valueWhenUnfulfilled);
	}

	public final Expression<? extends /* @ReadOnly */Boolean> getCondition() {
		return getOperand();
	}
}
