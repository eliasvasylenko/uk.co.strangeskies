/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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
package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;

import java.util.List;

import uk.co.strangeskies.reflection.ExecutableToken;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.ExpressionVisitor.ValueExpressionVisitor;

public class MethodExpression<O, T> implements ValueExpression<T> {
	private final ValueExpression<? extends O> receiver;
	private final ExecutableToken<O, T> invocable;
	private final List<ValueExpression<?>> arguments;

	protected MethodExpression(ValueExpression<? extends O> receiver, ExecutableToken<O, T> invocable,
			List<ValueExpression<?>> arguments) {
		this.receiver = receiver;
		this.invocable = invocable;
		this.arguments = arguments;
	}

	@Override
	public void accept(ValueExpressionVisitor<T> visitor) {
		visitor.visitMethod(receiver, invocable, arguments);
	}

	@Override
	public TypeToken<T> getType() {
		return invocable.getReturnType();
	}

	/**
	 * @see #invokeStatic(ExecutableToken, List)
	 */
	@SuppressWarnings("javadoc")
	public static <T> MethodExpression<Void, T> invokeStatic(ExecutableToken<Void, T> executable,
			ValueExpression<?>... arguments) {
		return invokeStatic(executable, asList(arguments));
	}

	/**
	 * @param <T>
	 *          the type of the result of the execution
	 * @param executable
	 *          the executable to be invoked
	 * @param arguments
	 *          the expressions of the arguments of the invocation
	 * @return an expression describing the invocation of the given static
	 *         executable with the given argument expressions
	 */
	public static <T> MethodExpression<Void, T> invokeStatic(ExecutableToken<Void, T> executable,
			List<ValueExpression<?>> arguments) {
		return new MethodExpression<>(null, executable, arguments);
	}
}
