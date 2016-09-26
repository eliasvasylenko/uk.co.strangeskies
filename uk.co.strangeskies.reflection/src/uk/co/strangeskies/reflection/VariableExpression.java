/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import uk.co.strangeskies.reflection.ExpressionVisitor.ValueExpressionVisitor;
import uk.co.strangeskies.reflection.ExpressionVisitor.VariableExpressionVisitor;

public interface VariableExpression<T> extends ValueExpression<T> {
	default ValueExpression<T> assign(ValueExpression<? extends T> value) {
		return new AssignmentExpression<>(this, value);
	}

	@Override
	default void accept(ValueExpressionVisitor<T> visitor) {
		accept(visitor.variable());
	}

	void accept(VariableExpressionVisitor<T> visitor);
}
