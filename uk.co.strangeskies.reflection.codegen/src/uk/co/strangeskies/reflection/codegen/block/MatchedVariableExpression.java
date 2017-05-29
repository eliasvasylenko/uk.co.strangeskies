package uk.co.strangeskies.reflection.codegen.block;

import uk.co.strangeskies.reflection.token.VariableMatcher;

class MatchedVariableExpression<T> implements VariableExpression<T> {
	private final VariableMatcher<?, T> matcher;

	public MatchedVariableExpression(VariableMatcher<?, T> matcher) {
		this.matcher = matcher;
	}

	@Override
	public void evaluate(Scope scope) {
		throw new UnsupportedOperationException();
	}
}
