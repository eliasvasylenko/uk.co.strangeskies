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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * The signature of a method according to Java language override rules. In other
 * words, the identity of a method as given by its name and erased parameter
 * types.
 * 
 * @author Elias N Vasylenko
 */
public class MethodSignature {
	private final String name;
	private final Class<?>[] parameterClasses;

	/**
	 * @param method
	 *          the method for which we require the signature
	 */
	public MethodSignature(Method method) {
		this(method.getName(), method.getParameterTypes());
	}

	/**
	 * @param name
	 *          the name of the method signature
	 * @param parameterClasses
	 *          the erased type of the method signature
	 */
	public MethodSignature(String name, Class<?>[] parameterClasses) {
		this.name = name;
		this.parameterClasses = parameterClasses;
	}

	/**
	 * @return the name of the method
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the erased parameter types of the method
	 */
	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (!(obj instanceof MethodSignature))
			return false;

		MethodSignature that = (MethodSignature) obj;

		return Arrays.equals(this.parameterClasses, that.parameterClasses) && Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(parameterClasses) ^ name.hashCode();
	}

	@Override
	public String toString() {
		return name + "(" + Arrays.toString(parameterClasses) + ")";
	}
}
