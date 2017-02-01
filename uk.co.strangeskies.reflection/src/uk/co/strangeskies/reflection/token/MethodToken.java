/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link MethodToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the receiver type of the executable
 * @param <R>
 *          the return type of the executable
 */
public class MethodToken<O, R> extends ExecutableToken<O, R> {
	protected MethodToken(Class<?> instance, Method method) {
		super(instance, method.getReturnType(), method);
	}

	protected MethodToken(
			TypeToken<? super O> receiverType,
			TypeToken<? extends R> returnType,
			List<ExecutableParameter> parameters,
			List<Type> typeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		super(receiverType, returnType, parameters, typeArguments, executable, variableArityInvocation);
	}

	@Override
	protected <P, S> ExecutableToken<P, S> withExecutableTokenData(
			TypeToken<? super P> receiverType,
			TypeToken<? extends S> returnType,
			List<ExecutableParameter> parameters,
			List<Type> typeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		return new MethodToken<>(receiverType, returnType, parameters, typeArguments, executable, variableArityInvocation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExecutableToken<? extends O, ? extends R> withTypeArguments() {
		if (isRaw()) {
			return withExecutableTokenData(
					getReceiverType().withTypeArguments(),
					(TypeToken<? extends R>) forType(getMember().getGenericReturnType()),
					Arrays
							.stream(getMember().getParameters())
							.map(p -> new ExecutableParameter(p, p.getParameterizedType()))
							.collect(toList()),
					asList(getMember().getTypeParameters()),
					getMember(),
					isVariableArityInvocation());
		} else {
			return this;
		}
	}

	@Override
	public Method getMember() {
		return (Method) super.getMember();
	}

	@Override
	public TypeToken<?> getOwningDeclaration() {
		return getReceiverType();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected R invokeImpl(O receiver, Object[] arguments)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (R) getMember().invoke(receiver, arguments);
	}
}
