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

public class MethodExpression<O, T, I> implements ValueExpression<T, I> {
	private final ValueExpression<? extends O, ? super I> value;
	private final InvocableMember<O, T> invocable;

	public MethodExpression(ValueExpression<? extends O, ? super I> value, InvocableMember<O, T> invocable) {
		this.value = value;
		this.invocable = invocable;
	}

	@Override
	public ValueResult<T> evaluate(State state) {
		O targetObject = value.evaluate(state).get();

		T result = invocable.invoke(targetObject);

		return () -> result;
	}

	@Override
	public TypeToken<T> getType() {
		return invocable.getReturnType();
	}

	@Override
	public Scope<? extends I> getScope() {
		// TODO Auto-generated method stub
		return null;
	}
}
