package uk.co.strangeskies.gears.mathematics.expression;

public class ConditionalForwardingExpression<O> extends CompoundExpression<O> {
	private final Expression<? extends /* @ReadOnly */Boolean> condition;
	private final Expression<? extends O> expressionWhenFulfilled;
	private final Expression<? extends O> expressionWhenUnfulfilled;

	public ConditionalForwardingExpression(
			Expression<? extends /* @ReadOnly */Boolean> condition,
			Expression<? extends O> expressionWhenFulfilled,
			Expression<? extends O> expressionWhenUnfulfilled) {
		super(condition);

		if (condition == expressionWhenFulfilled
				|| condition == expressionWhenUnfulfilled) {
			throw new IllegalArgumentException(
					"The Condition is the same reference as one or more other Expressions.");
		}

		this.condition = condition;
		this.expressionWhenFulfilled = expressionWhenFulfilled;
		this.expressionWhenUnfulfilled = expressionWhenUnfulfilled;

		if (condition.getValue()) {
			getDependencies().add(expressionWhenFulfilled);
		} else {
			getDependencies().add(expressionWhenUnfulfilled);
		}
	}

	@Override
	protected final O evaluate() {
		if (condition.getValue()) {
			getDependencies().remove(expressionWhenUnfulfilled);
			getDependencies().add(expressionWhenFulfilled);
			return expressionWhenFulfilled.getValue();
		} else {
			getDependencies().remove(expressionWhenFulfilled);
			getDependencies().add(expressionWhenUnfulfilled);
			return expressionWhenUnfulfilled.getValue();
		}
	}

	public final Expression<? extends /* @ReadOnly */Boolean> getCondition() {
		return condition;
	}

	public final Expression<? extends O> getExpressionWhenFulfilled() {
		return expressionWhenFulfilled;
	}

	public final Expression<? extends O> getExpressionWhenUnfulfilled() {
		return expressionWhenUnfulfilled;
	}
}
