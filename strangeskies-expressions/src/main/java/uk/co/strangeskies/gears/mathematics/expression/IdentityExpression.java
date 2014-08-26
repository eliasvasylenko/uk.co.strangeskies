package uk.co.strangeskies.gears.mathematics.expression;

import uk.co.strangeskies.gears.utilities.Property;

public class IdentityExpression<T> extends MutableExpression<T> implements
		Property<T, T> {
	private T value;

	public IdentityExpression() {
	}

	public IdentityExpression(T value) {
		this.value = value;
	}

	@Override
	public T set(T value) {
		T previous = this.value;
		this.value = value;
		postUpdate();
		return previous;
	}

	@Override
	public final T getValue() {
		return value;
	}

	@Override
	public final T get() {
		return getValue();
	}
}
