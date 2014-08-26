package uk.co.strangeskies.gears.mathematics.values;

public abstract class ContinuousValue<S extends ContinuousValue<S>> extends
		Value<S> {
	private static final long serialVersionUID = 1L;

	public ContinuousValue() {
	}

	public ContinuousValue(Value<?> value) {
		super((Number) value);
	}

	public ContinuousValue(Number value) {
		super(value);
	}
}
