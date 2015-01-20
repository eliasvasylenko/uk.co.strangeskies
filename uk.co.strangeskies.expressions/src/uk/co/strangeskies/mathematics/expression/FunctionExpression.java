/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

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
