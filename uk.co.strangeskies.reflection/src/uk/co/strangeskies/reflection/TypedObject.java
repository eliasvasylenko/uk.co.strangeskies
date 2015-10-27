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

/**
 * @author Elias N Vasylenko
 * 
 *         Facilitates the ability to track the exact type of an object in cases
 *         where it would normally be erased and so unavailable through
 *         reflection.
 *
 * @param <T>
 *          The type of the object instance to track.
 */
public class TypedObject<T> {
	private final TypeToken<T> type;
	private final T object;

	/**
	 * @param type
	 *          The exact type of an object to keep track of.
	 * @param object
	 *          An object reference of the given type.
	 */
	public TypedObject(TypeToken<T> type, T object) {
		this.type = type;
		this.object = object;
	}

	/**
	 * @return The type of the reference.
	 */
	public TypeToken<T> getType() {
		return type;
	}

	/**
	 * @return An object reference guaranteed to be of the given type.
	 */
	public T getObject() {
		return object;
	}
}