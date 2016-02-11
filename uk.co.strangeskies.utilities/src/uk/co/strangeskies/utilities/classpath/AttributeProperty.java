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
package uk.co.strangeskies.utilities.classpath;

public class AttributeProperty<T> {
	private final String name;
	private final PropertyType<T> type;
	private final T value;

	public AttributeProperty(String name, PropertyType<T> type, T value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public static <T> AttributeProperty<T> parseString(String name, PropertyType<T> type, String valueString) {
		return new AttributeProperty<>(name, type, type.parseString(valueString));
	}

	public String name() {
		return name;
	}

	public PropertyType<T> type() {
		return type;
	}

	public T value() {
		return value;
	}

	public String composeString() {
		return type.composeString(value);
	}
}
