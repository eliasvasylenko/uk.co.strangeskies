package uk.co.strangeskies.reflection;

public class LocalVariableExpression<T> implements VariableExpression<T> {
	private final Scope scope;
	private final TypeToken<T> type;

	public LocalVariableExpression(Scope scope, TypeToken<T> type) {
		this.scope = scope;
		this.type = type;
	}

	@Override
	public TypeToken<T> getType() {
		return type;
	}

	@Override
	public VariableResult<T> evaluate(State state) {
		return new VariableResult<T>() {
			@Override
			public T get() {
				return state.getEnclosingScopeVariableStore(scope).get(LocalVariableExpression.this);
			}

			@Override
			public void set(T value) {
				state.getEnclosingScopeVariableStore(scope).set(LocalVariableExpression.this, value);
			}
		};
	}
}
