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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link ConstructorToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the receiver type of the executable
 * @param <R>
 *          the return type of the executable
 */
public class ConstructorToken<O, R> extends ExecutableToken<O, R> {
	protected ConstructorToken(Class<?> instance, Constructor<?> constructor) {
		super(instance, constructor.getDeclaringClass(), constructor);
	}

	protected ConstructorToken(
			TypeToken<? super O> receiverType,
			TypeToken<? extends R> returnType,
			List<ExecutableParameter> methodParameters,
			List<Type> methodTypeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		super(receiverType, returnType, methodParameters, methodTypeArguments, executable, variableArityInvocation);
	}

	@Override
	protected <P, S> ExecutableToken<P, S> withExecutableTokenData(
			TypeToken<? super P> receiverType,
			TypeToken<? extends S> returnType,
			List<ExecutableParameter> parameters,
			List<Type> typeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		return new ConstructorToken<>(
				receiverType,
				returnType,
				parameters,
				typeArguments,
				executable,
				variableArityInvocation);
	}

	@Override
	public ExecutableToken<? extends O, ? extends R> withTypeArguments() {
		if (isRaw()) {
			return withExecutableTokenData(
					getReceiverType().withTypeArguments(),
					getReturnType().withTypeArguments(),
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

	@SuppressWarnings("unchecked")
	@Override
	public Constructor<? super R> getMember() {
		return (Constructor<? super R>) super.getMember();
	}

	@Override
	public TypeToken<?> getOwningDeclaration() {
		return getReturnType();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected R invokeImpl(O receiver, Object[] arguments)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (!getReceiverType().getType().equals(void.class)) {
			Object[] argumentsWithReceiver = new Object[arguments.length + 1];
			argumentsWithReceiver[0] = receiver;
			System.arraycopy(arguments, 0, argumentsWithReceiver, 1, arguments.length);
			arguments = argumentsWithReceiver;
		}

		return (R) getMember().newInstance(arguments);
	}
}
