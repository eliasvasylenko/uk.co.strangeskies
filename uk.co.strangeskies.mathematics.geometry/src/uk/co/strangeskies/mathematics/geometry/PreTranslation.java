/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import uk.co.strangeskies.mathematics.expression.BinaryExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;

public class PreTranslation<O>
		extends BinaryExpression<PreTranslation<O>, NonCommutativelyTranslatable<? extends O>, Vector<?, ?>, O> {
	public PreTranslation(Expression<?, ? extends NonCommutativelyTranslatable<? extends O>> firstOperand,
			Expression<?, ? extends Vector<?, ?>> secondOperand) {
		super(firstOperand, secondOperand, (a, b) -> a.getPreTranslated(b));
	}

	@Override
	public String toString() {
		return "(" + getFirstOperand() + " * " + getSecondOperand() + ")";
	}

	@Override
	public PreTranslation<O> copy() {
		return new PreTranslation<>(getFirstOperand(), getSecondOperand());
	}
}
