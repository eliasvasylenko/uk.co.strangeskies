/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.values.Value;

public class PreRotation2<O> extends
		BiFunctionExpression<NonCommutativelyRotatable2<? extends O>, Value<?>, O> {
	public PreRotation2(
			Expression<? extends NonCommutativelyRotatable2<? extends O>> firstOperand,
			Expression<? extends Value<?>> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getPreRotated(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}
}
