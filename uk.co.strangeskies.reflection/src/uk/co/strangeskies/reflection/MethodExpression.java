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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodExpression<O, T> implements ValueExpression<T> {
	private final ValueExpression<? extends O> value;
	private final InvocableMember<O, T> invocable;
	private final List<ValueExpression<?>> arguments;

	protected MethodExpression(ValueExpression<? extends O> value, InvocableMember<O, T> invocable,
			List<ValueExpression<?>> arguments) {
		this.value = value;
		this.invocable = invocable;
		this.arguments = arguments;
	}

	@Override
	public ValueResult<T> evaluate(State state) {
		O targetObject = value.evaluate(state).get();

		T result = invocable.invoke(targetObject,
				arguments.stream().map(a -> a.evaluate(state).get()).collect(Collectors.toList()));

		return () -> result;
	}

	@Override
	public TypeToken<T> getType() {
		return invocable.getReturnType();
	}

	public static <O, T, I> MethodExpression<O, T> invoke(ValueExpression<? extends O> value,
			InvocableMember<O, T> invocable, ValueExpression<?>... arguments) {
		return invokeMethod(value, invocable, Arrays.asList(arguments));
	}

	public static <O, T, I> MethodExpression<O, T> invokeMethod(ValueExpression<? extends O> value,
			InvocableMember<O, T> invocable, List<ValueExpression<?>> arguments) {
		return new MethodExpression<>(value, invocable, arguments);
	}
}
