package uk.co.strangeskies.utilities;

public class IdentityProperty<T> implements Property<T, T> {
	private T value;

	public IdentityProperty() {}

	public IdentityProperty(T value) {
		this.value = value;
	}

	@Override
	public T set(IdentityProperty<T> this, T to) {
		value = to;
		return value;
	}

	@Override
	public T get() {
		return value;
	}
}
