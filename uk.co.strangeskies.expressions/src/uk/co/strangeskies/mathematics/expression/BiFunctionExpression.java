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

import java.util.function.BiFunction;

public class BiFunctionExpression<O1, O2, R> extends CompoundExpression<R> {
	private Expression<? extends O1> firstOperand;
	private Expression<? extends O2> secondOperand;
	private Expression<? extends BiFunction<? super O1, ? super O2, ? extends R>> operation;

	public BiFunctionExpression(
			Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			Expression<? extends BiFunction<? super O1, ? super O2, ? extends R>> operation) {
		super(firstOperand, secondOperand);

		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;

		this.operation = operation;
	}

	public BiFunctionExpression(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			BiFunction<? super O1, ? super O2, ? extends R> operation) {
		this(firstOperand, secondOperand, Expression.immutable(operation));
	}

	public Expression<? extends BiFunction<? super O1, ? super O2, ? extends R>> getOperation() {
		return operation;
	}

	public Expression<? extends O1> getFirstOperand() {
		return firstOperand;
	}

	public Expression<? extends O2> getSecondOperand() {
		return secondOperand;
	}

	public void setOperands(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand) {
		if (this.firstOperand != firstOperand) {
			if (this.secondOperand != secondOperand) {
				getDependencies().remove(this.firstOperand);
				getDependencies().remove(this.secondOperand);

				this.firstOperand = firstOperand;
				this.secondOperand = secondOperand;

				getDependencies().add(this.firstOperand);
				getDependencies().add(this.secondOperand);

				update();
			} else {
				setFirstOperand(firstOperand);
			}
		} else {
			if (this.secondOperand != secondOperand) {
				setSecondOperand(secondOperand);
			}
		}
	}

	public void setFirstOperand(Expression<? extends O1> operand) {
		if (this.firstOperand != operand) {
			if (firstOperand != secondOperand) {
				getDependencies().remove(firstOperand);
			}

			firstOperand = operand;
			getDependencies().add(firstOperand);

			update();
		}
	}

	public void setSecondOperand(Expression<? extends O2> operand) {
		if (this.secondOperand != operand) {
			if (firstOperand != secondOperand) {
				getDependencies().remove(secondOperand);
			}

			secondOperand = operand;
			getDependencies().add(secondOperand);

			update();
		}
	}

	@Override
	protected R evaluate() {
		return operation.getValue().apply(firstOperand.getValue(),
				secondOperand.getValue());
	}
}
