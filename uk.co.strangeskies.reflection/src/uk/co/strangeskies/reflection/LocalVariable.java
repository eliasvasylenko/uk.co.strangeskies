package uk.co.strangeskies.reflection;

public class LocalVariable<T> {
	private final TypeToken<T> type;

	public LocalVariable(TypeToken<T> type) {
		this.type = type;
	}

	public TypeToken<T> getType() {
		return type;
	}
}
