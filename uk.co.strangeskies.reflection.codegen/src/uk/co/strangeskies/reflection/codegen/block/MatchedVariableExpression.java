package uk.co.strangeskies.reflection.codegen.block;

import uk.co.strangeskies.reflection.codegen.block.ExpressionVisitor.VariableExpressionVisitor;
import uk.co.strangeskies.reflection.token.VariableMatcher;

class MatchedVariableExpression<T> implements VariableExpression<T> {
	private final VariableMatcher<?, T> matcher;

	public MatchedVariableExpression(VariableMatcher<?, T> matcher) {
		this.matcher = matcher;
	}

	@Override
	public void accept(VariableExpressionVisitor<T> visitor) {
		throw new UnsupportedOperationException();
	}
}
