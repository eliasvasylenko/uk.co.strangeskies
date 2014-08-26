package uk.co.strangeskies.gears.mathematics.expression;

import uk.co.strangeskies.gears.utilities.function.TriFunction;

public abstract class TriFunctionExpression<O1, O2, O3, R> extends
		CompoundExpression<R> {
	private Expression<? extends O1> firstOperand;
	private Expression<? extends O2> secondOperand;
	private Expression<? extends O3> thirdOperand;
	private Expression<? extends TriFunction<? super O1, ? super O2, ? super O3, ? extends R>> operation;

	public TriFunctionExpression(
			Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			Expression<? extends O3> thirdOperand,
			Expression<? extends TriFunction<? super O1, ? super O2, ? super O3, ? extends R>> operation) {
		super(firstOperand, secondOperand, thirdOperand);

		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
		this.thirdOperand = thirdOperand;

		this.operation = operation;
	}

	public TriFunctionExpression(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			Expression<? extends O3> thirdOperand,
			TriFunction<? super O1, ? super O2, ? super O3, ? extends R> operation) {
		this(firstOperand, secondOperand, thirdOperand, Expression
				.immutable(operation));
	}

	public Expression<? extends TriFunction<? super O1, ? super O2, ? super O3, ? extends R>> getOperation() {
		return operation;
	}

	public Expression<? extends O1> getFirstOperand() {
		return firstOperand;
	}

	public Expression<? extends O2> getSecondOperand() {
		return secondOperand;
	}

	public Expression<? extends O3> getThirdOperand() {
		return thirdOperand;
	}

	public void setOperands(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			Expression<? extends O3> thirdOperand) {
		if (this.firstOperand != firstOperand
				|| this.secondOperand != secondOperand
				|| this.thirdOperand != thirdOperand) {
			getDependencies().remove(this.firstOperand);
			getDependencies().remove(this.secondOperand);
			getDependencies().remove(this.thirdOperand);

			this.firstOperand = firstOperand;
			this.secondOperand = secondOperand;
			this.thirdOperand = thirdOperand;

			getDependencies().add(this.firstOperand);
			getDependencies().add(this.secondOperand);
			getDependencies().add(this.thirdOperand);

			update();
		}
	}

	public void setFirstOperand(Expression<? extends O1> operand) {
		if (firstOperand != operand) {
			if (firstOperand != secondOperand && firstOperand != thirdOperand) {
				getDependencies().remove(firstOperand);
			}

			firstOperand = operand;
			getDependencies().add(firstOperand);

			update();
		}
	}

	public void setSecondOperand(Expression<? extends O2> operand) {
		if (secondOperand != operand) {
			if (firstOperand != secondOperand && secondOperand != thirdOperand) {
				getDependencies().remove(secondOperand);
			}

			secondOperand = operand;
			getDependencies().add(secondOperand);

			update();
		}
	}

	public void setThirdOperand(Expression<? extends O3> operand) {
		if (thirdOperand != operand) {
			if (firstOperand != thirdOperand && secondOperand != thirdOperand) {
				getDependencies().remove(secondOperand);
			}

			thirdOperand = operand;
			getDependencies().add(secondOperand);

			update();
		}
	}

	@Override
	protected R evaluate() {
		return operation.getValue().apply(firstOperand.getValue(),
				secondOperand.getValue(), thirdOperand.getValue());
	}
}
