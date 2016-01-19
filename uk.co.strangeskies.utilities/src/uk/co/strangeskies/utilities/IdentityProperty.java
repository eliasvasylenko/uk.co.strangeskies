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
 * A basic implementation of {@link Property} which simple stores it's value as
 * a member variable, which can be updated and retrieved through get and set.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the property.
 */
/* @I */
public class IdentityProperty<T> implements Property<T, T> {
	private/* @I */T value;

	/**
	 * Create an IndentityProperty with null as the initial value.
	 */
	public IdentityProperty() {}

	/**
	 * Create an identity with the given initial value.
	 * 
	 * @param value
	 *          The initial value for this property.
	 */
	public IdentityProperty(T value) {
		this.value = value;
	}

	@Override
	public T set(/* @Mutable IdentityProperty<T> this, */T to) {
		value = to;
		return value;
	}

	@Override
	public/* @I */T get() {
		return value;
	}
}
