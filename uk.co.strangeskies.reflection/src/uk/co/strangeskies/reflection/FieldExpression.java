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

public class FieldExpression<O, T> implements VariableExpression<T> {
	private final ValueExpression<? extends O> value;
	private final FieldMember<O, T> field;

	protected FieldExpression(ValueExpression<? extends O> value, FieldMember<O, T> field) {
		this.value = value;
		this.field = field;
	}

	@Override
	public void accept(ValueExpressionVisitor<T> visitor) {
		accept(visitor.variable());
	}

	@Override
	public void accept(VariableExpressionVisitor<T> visitor) {
		visitor.visitField(value, field);
	}

	@Override
	public TypeToken<T> getType() {
		return field.getFieldType();
	}
}
