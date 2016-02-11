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
