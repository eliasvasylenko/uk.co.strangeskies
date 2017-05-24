package uk.co.strangeskies.reflection;

public class AnnotationProperty {
	private final String name;
	private final Object value;

	public AnnotationProperty(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String name() {
		return name;
	}

	public Object value() {
		return value;
	}
}
