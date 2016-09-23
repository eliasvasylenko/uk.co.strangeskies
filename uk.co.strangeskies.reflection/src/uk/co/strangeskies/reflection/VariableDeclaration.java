package uk.co.strangeskies.reflection;

public class VariableDeclaration<T> implements Statement {
	private final LocalValueExpression<T> value;
	private final ValueExpression<? extends T> initializer;

	public VariableDeclaration(LocalValueExpression<T> value, ValueExpression<? extends T> initializer) {
		this.value = value;
		this.initializer = initializer;
	}

	@Override
	public void execute(State state) {
		state.declareLocal(value, initializer.evaluate(state).get());
	}
}
