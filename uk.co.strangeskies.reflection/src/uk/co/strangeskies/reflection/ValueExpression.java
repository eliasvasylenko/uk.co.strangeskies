package uk.co.strangeskies.reflection;

public interface ValueExpression<T> extends Expression {
	@Override
	ValueResult<T> evaluate(EvaluationScope scope);

	TypeToken<T> getType();
}
