/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public interface InvocationResolver<T> {
	public TypeLiteral<T> getType();

	public List<Type> inferTypes(Executable executable, Type result,
			Type... parameters);

	public boolean validateInvocation(Executable executable, Type result,
			Type... parameters);

	public Method resolveOverload(String methodName, Type... parameters);

	public boolean validateParameterization(Executable executable,
			Type... typeArguments);

	public Object invokeWithParameterization(Executable executable,
			Type[] typeArguments, T receiver, Object... parameters);

	public Object invokeSafely(Executable executable, T receiver,
			Object... parameters);
}
