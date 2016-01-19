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

import uk.co.strangeskies.utilities.Copyable;

/**
 * A basic interface extension of {@link Expression} providing a default
 * implementation of {@link #decoupleValue()} which simply copies the result of
 * {@link #getValue()}.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public interface CopyDecouplingExpression<T extends Copyable<T>> extends
		Expression<T> {
	@Override
	public default T decoupleValue() {
		return getValue().copy();
	}
}
