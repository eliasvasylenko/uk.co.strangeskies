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

		Parser<String> singleQuotedString = matching("([^\\'\\\\]*(\\\\.[^\'\\\\]*)*)").append("\\'").prepend("\\'");

		Parser<String> doubleQuotedString = matching("([^\"\\\\]*(\\\\.[^\"\\\\]*)*)").append("\"").prepend("\"");

		valueString = matching("[_a-zA-Z0-9\\.]*")
				.orElse(singleQuotedString.orElse(doubleQuotedString).transform(StringEscaper.java()::unescape));

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
