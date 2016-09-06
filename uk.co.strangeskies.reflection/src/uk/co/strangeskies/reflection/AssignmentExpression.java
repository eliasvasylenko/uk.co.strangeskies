package uk.co.strangeskies.reflection;

public class AssignmentExpression<T> implements ValueExpression<T> {
	private final VariableExpression<T> target;
	private final ValueExpression<? extends T> value;

	public AssignmentExpression(VariableExpression<T> target, ValueExpression<? extends T> value) {
		this.target = target;
		this.value = value;
	}

	@Override
	public ValueResult<T> evaluate(EvaluationScope scope) {
		VariableResult<T> targetResult = target.evaluate(scope);

		T result = value.evaluate(scope).get();

		targetResult.set(result);

		return () -> result;
	}

	@Override
	public TypeToken<T> getType() {
		return target.getType();
	}
}
