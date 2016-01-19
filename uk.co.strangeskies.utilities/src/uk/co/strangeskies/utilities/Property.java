/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

/**
 * This interface represents a gettable and settable property of a given type.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the property.
 * @param <R>
 *          The supertype of the property type with which we can set a property.
 *          Commonly this is the same as {@code T}.
 */
/* @I */
public interface Property<T extends R, R> {
	/**
	 * Set the value of this property to the given value.
	 * 
	 * @param to
	 *          The new value to set for this property.
	 * @return The previous value of this property.
	 */
	public T set(/* @Mutable Property<T, R> this, */R to);

	/**
	 * Get the current value of the property.
	 * 
	 * @return The current value.
	 */
	public/* @I */T get();
}
