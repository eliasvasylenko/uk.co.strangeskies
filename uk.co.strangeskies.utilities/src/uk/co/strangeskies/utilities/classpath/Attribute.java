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

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import uk.co.strangeskies.utilities.text.StringEscaper;

public class Attribute {
	private final String name;
	private final Map<String, AttributeProperty<?>> properties;

	public Attribute(String name, AttributeProperty<?>... attributes) {
		this(name, Arrays.asList(attributes));
	}

	public Attribute(String name, Collection<? extends AttributeProperty<?>> attributes) {
		this.name = name;
		this.properties = Collections
				.unmodifiableMap(attributes.stream().collect(toMap(AttributeProperty::name, Function.identity())));
	}

	public String name() {
		return name;
	}

	public Map<String, AttributeProperty<?>> properties() {
		return properties;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ properties.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}

		Attribute otherAttribute = (Attribute) obj;

		return Objects.equals(name, otherAttribute.name()) && Objects.equals(properties, otherAttribute.properties());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(name);

		for (AttributeProperty<?> property : properties.values()) {
			String valueText;

			if (property.value() != null) {
				String value = property.composeString();
				String escapedValue = StringEscaper.java().escape(value);
				if (!escapedValue.equals(value)) {
					value = "\"" + escapedValue + "\"";
				}
				valueText = "=" + value;
			} else {
				valueText = "";
			}
			builder.append(";" + property.name() + valueText);
		}

		return builder.toString();
	}
}
