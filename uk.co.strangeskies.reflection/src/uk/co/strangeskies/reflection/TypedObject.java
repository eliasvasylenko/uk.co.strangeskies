/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.Objects;

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
public class TypedObject<T> implements Reified<TypedObject<T>> {
	private final TypeToken<T> type;
	private final T object;

	/**
	 * @param type
	 *          The exact type of an object to keep track of.
	 * @param object
	 *          An object reference of the given type.
	 */
	public TypedObject(TypeToken<T> type, T object) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(object);

		this.type = type;
		this.object = object;
	}

	/**
	 * Cast an untyped object into a typed object.
	 * 
	 * @param <T>
	 *          The target type of the cast
	 * @param type
	 *          A type token over the type of the cast
	 * @param object
	 *          The object to cast
	 * @return A typed object over the given type and object
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypedObject<T> castInto(TypeToken<T> type, Object object) {
		return new TypedObject<>(type, (T) object);
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

	@Override
	public String toString() {
		return object + ": " + type;
	}

	@Override
	public TypedObject<T> copy() {
		return this;
	}

	@Override
	public TypeToken<TypedObject<T>> getThisType() {
		return new TypeToken<TypedObject<T>>() {}.withTypeArgument(new TypeParameter<T>(), type);
	}
}
