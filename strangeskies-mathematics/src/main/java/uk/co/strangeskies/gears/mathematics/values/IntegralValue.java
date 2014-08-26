package uk.co.strangeskies.gears.mathematics.values;

public abstract class IntegralValue<S extends IntegralValue<S>> extends
		Value<S> {
	private static final long serialVersionUID = 1L;

	public IntegralValue() {
	}

	public IntegralValue(Value<?> value) {
		super(value);
	}

	public IntegralValue(Number value) {
		super(value);
	}
}
