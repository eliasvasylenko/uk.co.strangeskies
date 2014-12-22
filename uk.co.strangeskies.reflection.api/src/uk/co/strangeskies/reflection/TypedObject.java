package uk.co.strangeskies.reflection;

public class TypedObject<T> {
	private final TypeLiteral<T> type;
	private final T object;

	public TypedObject(TypeLiteral<T> type, T object) {
		this.type = type;
		this.object = object;
	}

	public TypeLiteral<T> getType() {
		return type;
	}

	public T getObject() {
		return object;
	}
}
