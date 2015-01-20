/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import uk.co.strangeskies.utilities.function.TriFunction;

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
