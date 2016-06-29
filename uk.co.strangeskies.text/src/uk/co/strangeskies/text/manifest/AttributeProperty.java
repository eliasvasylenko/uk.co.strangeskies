/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.manifest;

import java.util.Objects;

public class AttributeProperty<T> {
	private final String name;
	private final PropertyType<T> type;
	private final T value;

	public AttributeProperty(String name, PropertyType<T> type, T value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public static <T> AttributeProperty<T> parseValueString(String name, PropertyType<T> type, String valueString) {
		return new AttributeProperty<>(name, type, type.parseString(valueString));
	}

	public static AttributeProperty<?> untyped(String name, Object value) {
		return new AttributeProperty<>(name, null, value);
	}

	public String composeValueString() {
		String valueString;
		if (type != null) {
			valueString = type.composeString(value);
		} else {
			valueString = value.toString();
		}

		return valueString;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name);

		if (type != null) {
			builder.append(":").append(type.name());
		}
		if (value != null) {
			String valueString = composeValueString();

			if (!valueString.matches(ManifestAttributeParser.SIMPLE_VALUE)) {
				valueString = "\"" + ManifestAttributeParser.DOUBLE_QUOTE_ESCAPER.escape(valueString) + "\"";
			}

			builder.append("=").append(valueString);
		}

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name) ^ Objects.hashCode(type) ^ Objects.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}

		AttributeProperty<?> otherProperty = (AttributeProperty<?>) obj;

		return Objects.equals(name, otherProperty.name()) && Objects.equals(type, otherProperty.type())
				&& Objects.equals(value, otherProperty.value());
	}
}
