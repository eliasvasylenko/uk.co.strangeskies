package uk.co.strangeskies.reflection;

public interface VariableExpression<T> extends ValueExpression<T> {
	@Override
	VariableResult<T> evaluate(EvaluationScope context);
}
