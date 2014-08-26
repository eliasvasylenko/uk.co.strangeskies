package uk.co.strangeskies.gears.mathematics.expression;

import java.util.function.Function;

public class FunctionExpression<O, R> extends CompoundExpression<R> {
	private Expression<? extends O> operand;
	private final Expression<? extends Function<? super O, ? extends R>> operation;

	public FunctionExpression(Expression<? extends O> operand,
			Expression<? extends Function<? super O, ? extends R>> operation) {
		super(operand, operation);

		this.operand = operand;

		this.operation = operation;
	}

	public FunctionExpression(Expression<? extends O> operand,
			Function<? super O, ? extends R> operation) {
		super(operand);

		this.operand = operand;

		this.operation = Expression.immutable(operation);
	}

	public Expression<? extends Function<? super O, ? extends R>> getOperation() {
		return operation;
	}

	public Expression<? extends O> getOperand() {
		return operand;
	}

	public void setOperand(Expression<? extends O> operand) {
		if (this.operand != operand) {
			getDependencies().remove(this.operand);

			this.operand = operand;
			getDependencies().add(this.operand);

			update();
		}
	}

	@Override
	protected R evaluate() {
		return operation.getValue().apply(operand.getValue());
	}
}
