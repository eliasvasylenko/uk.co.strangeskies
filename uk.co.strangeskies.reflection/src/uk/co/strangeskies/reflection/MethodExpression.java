package uk.co.strangeskies.reflection;

public class MethodExpression<O, T> implements ValueExpression<T> {
	private final ValueExpression<? extends O> value;
	private final InvocableMember<O, T> invocable;

	public MethodExpression(ValueExpression<? extends O> value, InvocableMember<O, T> invocable) {
		this.value = value;
		this.invocable = invocable;
	}

	@Override
	public ValueResult<T> evaluate(EvaluationScope scope) {
		O targetObject = value.evaluate(scope).get();

		T result = invocable.invoke(targetObject);

		return () -> result;
	}

	@Override
	public TypeToken<T> getType() {
		return invocable.getReturnType();
	}
}
