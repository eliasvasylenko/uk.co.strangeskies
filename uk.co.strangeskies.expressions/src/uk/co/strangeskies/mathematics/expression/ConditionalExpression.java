package uk.co.strangeskies.mathematics.expression;


public class ConditionalExpression<O> extends
		FunctionExpression< Boolean, O> {

	public ConditionalExpression(
			Expression<? extends  Boolean> condition,
			final O valueWhenFulfilled, final O valueWhenUnfulfilled) {
		super(condition, t -> t ? valueWhenFulfilled : valueWhenUnfulfilled);
	}

	public final Expression<? extends  Boolean> getCondition() {
		return getOperand();
	}
}
