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

import static uk.co.strangeskies.utilities.text.Parser.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.utilities.text.Parser;
import uk.co.strangeskies.utilities.text.StringEscaper;
import uk.co.strangeskies.utilities.tuple.Pair;

public class ManifestAttributeParser {
	private final Parser<? extends List<? extends Attribute>> attributes;
	private final Parser<? extends Attribute> attribute;
	private final Parser<? extends AttributeProperty<?>> attributeProperty;
	private final Parser<? extends String> valueString;

	public ManifestAttributeParser(PropertyType<?>... knownPropertyTypes) {
		this(Arrays.asList(knownPropertyTypes));
	}

	public ManifestAttributeParser(Collection<? extends PropertyType<?>> knownPropertyTypes) {
		List<PropertyType<?>> finalKnownPropertyTypes = new ArrayList<>(knownPropertyTypes);

		Parser<PropertyType<?>> type = matching("[_a-zA-Z0-9<>]*")
				.transform(s -> PropertyType.fromName(s, finalKnownPropertyTypes));

		Parser<String> singleQuotedString = matching("([^\\'\\\\]*(\\\\.[^\'\\\\]*)*)")
				.transform(StringEscaper.java()::unescape).append("\\'").prepend("\\'");

		Parser<String> doubleQuotedString = matching("([^\"\\\\]*(\\\\.[^\"\\\\]*)*)")
				.transform(StringEscaper.java()::unescape).append("\"").prepend("\"");

		valueString = singleQuotedString.orElse(doubleQuotedString).orElse(matching("[_a-zA-Z0-9\\.]*"));

		attributeProperty = matching("[_a-zA-Z0-9\\.]*")
				.appendTransform(type.prepend(":").orElse(PropertyType.STRING), this::newPair).append("=")
				.appendTransform(valueString, this::newAttributeProperty);

		attribute = matching("[_a-zA-Z0-9\\.]*").appendTransform(
				Parser.list(attributeProperty, "\\s*;\\s*", 0).prepend("\\s*;\\s*").orElse(Collections.emptyList()),
				Attribute::new);

		attributes = Parser.list(attribute, "\\s*,\\s*");
	}

	private Pair<String, PropertyType<?>> newPair(String name, PropertyType<?> type) {
		return new Pair<>(name, type);
	}

	private AttributeProperty<?> newAttributeProperty(Pair<String, PropertyType<?>> nameAndType, String valueString) {
		return AttributeProperty.parseString(nameAndType.getLeft(), nameAndType.getRight(), valueString);
	}

	public String parseValueString(String valueString) {
		return this.valueString.parse(valueString);
	}

	public AttributeProperty<?> parseAttributeProperty(String attribute) {
		return this.attributeProperty.parse(attribute);
	}

	public Attribute parseAttribute(String entry) {
		return this.attribute.parse(entry);
	}

	public List<Attribute> parseAttributes(String entry) {
		return new ArrayList<>(this.attributes.parse(entry));
	}
}
