package uk.co.strangeskies.gears.utilities;

public class IdentityProperty<T> implements SimpleProperty<T> {
	private T value;

	public IdentityProperty() {
	}

	public IdentityProperty(T value) {
		this.value = value;
	}

	@Override
	public T set(T to) {
		return value = to;
	}

	@Override
	public T get() {
		return value;
	}
}
