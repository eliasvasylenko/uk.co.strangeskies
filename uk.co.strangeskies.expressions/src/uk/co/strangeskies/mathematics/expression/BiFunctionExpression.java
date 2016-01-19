/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

/**
 * As {@link FunctionExpression}, but with two operands.
 *
 * @author Elias N Vasylenko
 * @param <O1>
 *          The type of the first operand.
 * @param <O2>
 *          The type of the second operand.
 * @param <R>
 *          The type of the result.
 */
public class BiFunctionExpression<O1, O2, R> extends DependentExpression<R> {
	private Expression<? extends O1> firstOperand;
	private Expression<? extends O2> secondOperand;
	private Expression<? extends BiFunction<? super O1, ? super O2, ? extends R>> operation;

	/**
	 * @param firstOperand
	 *          An expression providing the first operand for the function.
	 * @param secondOperand
	 *          An expression providing the second operand for the function.
	 * @param operation
	 *          A expression providing a function transforming the operands into a
	 *          value of this expression's type.
	 */
	public BiFunctionExpression(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			Expression<? extends BiFunction<? super O1, ? super O2, ? extends R>> operation) {
		super(firstOperand, secondOperand);

		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;

		this.operation = operation;
	}

	/**
	 * @param firstOperand
	 *          An expression providing the first operand for the function.
	 * @param secondOperand
	 *          An expression providing the second operand for the function.
	 * @param operation
	 *          A function transforming the operands into a value of this
	 *          expression's type.
	 */
	public BiFunctionExpression(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand,
			BiFunction<? super O1, ? super O2, ? extends R> operation) {
		this(firstOperand, secondOperand, Expression.immutable(operation));
	}

	/**
	 * @return The first operand expression.
	 */
	public Expression<? extends O1> getFirstOperand() {
		return firstOperand;
	}

	/**
	 * @return The second operand expression.
	 */
	public Expression<? extends O2> getSecondOperand() {
		return secondOperand;
	}

	/**
	 * @param firstOperand
	 *          A new first operand.
	 * @param secondOperand
	 *          A new second operand.
	 */
	public void setOperands(Expression<? extends O1> firstOperand,
			Expression<? extends O2> secondOperand) {
		try {
			getWriteLock().lock();
			if (this.firstOperand != firstOperand
					|| this.secondOperand != secondOperand) {
				getDependencies().remove(this.firstOperand);
				getDependencies().remove(this.secondOperand);

				this.firstOperand = firstOperand;
				this.secondOperand = secondOperand;

				getDependencies().add(this.firstOperand);
				getDependencies().add(this.secondOperand);

				postUpdate();
			}
		} finally {
			unlockWriteLock();
		}
	}

	/**
	 * @param operand
	 *          A new first operand.
	 */
	public void setFirstOperand(Expression<? extends O1> operand) {
		try {
			getWriteLock().lock();
			if (firstOperand != operand) {
				if (firstOperand != secondOperand) {
					getDependencies().remove(firstOperand);
				}

				firstOperand = operand;
				getDependencies().add(firstOperand);
				postUpdate();
			}
		} finally {
			unlockWriteLock();
		}
	}

	/**
	 * @param operand
	 *          A new second operand.
	 */
	public void setSecondOperand(Expression<? extends O2> operand) {
		try {
			getWriteLock().lock();
			if (secondOperand != operand) {
				if (firstOperand != secondOperand) {
					getDependencies().remove(secondOperand);
				}

				secondOperand = operand;
				getDependencies().add(secondOperand);

				postUpdate();
			}
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	protected R evaluate() {
		return operation.getValue().apply(firstOperand.getValue(),
				secondOperand.getValue());
	}
}
