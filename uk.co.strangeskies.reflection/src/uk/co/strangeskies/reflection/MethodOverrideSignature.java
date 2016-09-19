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
import java.util.Objects;

/**
 * The signature of a method according to Java language override rules. In other
 * words, the identity of a method as given by its name and erased parameter
 * types.
 * 
 * @author Elias N Vasylenko
 */
public class MethodOverrideSignature {
	private final String name;
	private final Class<?>[] parameterClasses;

	/**
	 * @param name
	 *          the name of the method signature
	 * @param parameterClasses
	 *          the erased type of the method signature
	 */
	public MethodOverrideSignature(String name, Class<?>[] parameterClasses) {
		this.name = name;
		this.parameterClasses = parameterClasses;
	}

	public String getName() {
		return name;
	}

	public Class<?>[] getParameterClasses() {
		return parameterClasses;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		else if (!(obj instanceof MethodOverrideSignature))
			return false;

		MethodOverrideSignature that = (MethodOverrideSignature) obj;

		return Arrays.equals(this.parameterClasses, that.parameterClasses) && Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(parameterClasses) ^ name.hashCode();
	}

	@Override
	public String toString() {
		return name + "/" + Arrays.toString(parameterClasses);
	}
}
