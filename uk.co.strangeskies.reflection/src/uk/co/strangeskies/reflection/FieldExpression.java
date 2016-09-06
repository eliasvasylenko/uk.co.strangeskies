package uk.co.strangeskies.reflection;

public class FieldExpression<O, T> implements VariableExpression<T> {
	private final ValueExpression<? extends O> value;
	private final FieldMember<O, T> field;

	public FieldExpression(ValueExpression<? extends O> value, FieldMember<O, T> field) {
		this.value = value;
		this.field = field;
	}

	@Override
	public VariableResult<T> evaluate(EvaluationScope scope) {
		O targetObject = value.evaluate(scope).get();

		return new VariableResult<T>() {
			@Override
			public T get() {
				return field.get(targetObject);
			}

			@Override
			public void set(T value) {
				field.set(targetObject, value);
			}
		};
	}

	@Override
	public TypeToken<T> getType() {
		return field.getFieldType();
	}
}
