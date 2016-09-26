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

public class TypedBlockDefinition<T> extends Block<TypedBlockDefinition<T>> {
	public TypedBlockDefinition() {
		super();
	}

	public TypedBlockDefinition(TypedBlockDefinition<T> scope) {
		super(scope);
	}

	public TypedBlockDefinition<T> addReturnStatement(ValueExpression<T> expression) {
		addStatement(v -> v.visitReturn(expression));

		return this;
	}

	@Override
	public TypedBlockDefinition<T> copy() {
		return new TypedBlockDefinition<>(this);
	}
}
