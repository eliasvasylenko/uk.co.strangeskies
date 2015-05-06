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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * A collection of utility methods relating to generic array types.
 * 
 * @author Elias N Vasylenko
 */
public class GenericArrayTypes {
	private GenericArrayTypes() {}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType fromComponentType(Type component) {
		return new GenericArrayType() {
			@Override
			public Type getGenericComponentType() {
				return component;
			}

			@Override
			public String toString() {
				return Types.toString(component)
						+ (component instanceof IntersectionType ? " " : "") + "[]";
			}

			@Override
			public boolean equals(Object object) {
				if (this == object)
					return true;
				if (object == null || !(object instanceof GenericArrayType))
					return false;

				GenericArrayType that = (GenericArrayType) object;

				return component.equals(that.getGenericComponentType());
			}

			@Override
			public int hashCode() {
				return component.hashCode();
			}
		};
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType fromComponentType(Type component,
			int arrayDimensions) {
		GenericArrayType array;

		do {
			component = array = fromComponentType(component);
		} while (--arrayDimensions > 0);

		return array;
	}
}
