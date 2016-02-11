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
