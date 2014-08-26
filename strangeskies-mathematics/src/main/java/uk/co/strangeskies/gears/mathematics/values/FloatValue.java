package uk.co.strangeskies.gears.mathematics.values;

public final class FloatValue extends ContinuousValue<FloatValue> {
	private static final long serialVersionUID = 5916143538083079342L;

	private float value;

	public FloatValue() {
	}

	public FloatValue(Value<?> value) {
		super((Number) value);
	}

	public FloatValue(Number value) {
		super(value);
	}

	@Override
	public final FloatValue reciprocate() {
		value = 1 / value;

		return this;
	}

	@Override
	public final FloatValue add(Value<?> value) {
		this.value += value.doubleValue();
		update();
		return this;
	}

	@Override
	public final FloatValue subtract(Value<?> value) {
		this.value -= value.doubleValue();
		update();
		return this;
	}

	@Override
	public final FloatValue negate() {
		value = -value;
		update();
		return this;
	}

	@Override
	public final FloatValue multiply(int value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final FloatValue multiply(long value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final FloatValue multiply(float value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final FloatValue multiply(double scalar) {
		this.value *= scalar;
		update();
		return this;
	}

	@Override
	public final FloatValue divide(int value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final FloatValue divide(long value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final FloatValue divide(float value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final FloatValue divide(double value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final FloatValue divide(Value<?> value) {
		this.value = value.getDividedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final double doubleValue() {
		return value;
	}

	@Override
	public final float floatValue() {
		return value;
	}

	@Override
	public final int intValue() {
		return (int) value;
	}

	@Override
	public final long longValue() {
		return (long) value;
	}

	@Override
	public final String toString() {
		return new Double(value).toString();
	}

	@Override
	public final FloatValue set(Value<?> value) {
		this.value = value.floatValue();
		update();
		return this;
	}

	@Override
	public final FloatValue setValue(Number value) {
		this.value = value.floatValue();
		update();
		return this;
	}

	@Override
	public final FloatValue multiply(Value<?> value) {
		this.value = value.getMultipliedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final int compareToAtSupportedPrecision(Value<?> other) {
		return new Float(this.value).compareTo(new Float(other.floatValue()));
	}

	@Override
	public final boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that instanceof Value<?>) {
			return equals((Value<?>) that);
		}
		if (that instanceof Number) {
			return ((Number) that).equals(this.value);
		}
		return false;
	}

	@Override
	protected final boolean equals(Value<?> that) {
		return ((Value<?>) that).equals(this.value);
	}

	@Override
	public final int hashCode() {
		return new Float(value).hashCode();
	}

	@Override
	public final boolean equals(double value) {
		return this.value == value;
	}

	@Override
	public final boolean equals(float value) {
		return this.value == value;
	}

	@Override
	public final boolean equals(int value) {
		return this.value == value;
	}

	@Override
	public final boolean equals(long value) {
		return this.value == value;
	}

	@Override
	public final FloatValue increment() {
		value++;
		update();
		return this;
	}

	@Override
	public final FloatValue decrement() {
		value--;
		update();
		return this;
	}

	@Override
	public final FloatValue copy() {
		return new FloatValue(this);
	}

	@Override
	public final FloatValue unitInTheLastPlaceAbove() {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return this;
		}
		double absoluteValue = Math.abs(value);

		long nextValueLong = Double.doubleToLongBits(absoluteValue) + 1;
		double nextValue = Double.longBitsToDouble(nextValueLong);

		// if ended on bad number go down instead
		if (Double.isNaN(nextValue) || Double.isInfinite(nextValue)) {
			nextValueLong = nextValueLong - 2;
			nextValue = absoluteValue;
			absoluteValue = Double.longBitsToDouble(nextValueLong);
		}

		return new FloatValue(Math.abs(nextValue - absoluteValue));
	}

	@Override
	public final FloatValue unitInTheLastPlaceBelow() {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return this;
		}
		double absoluteValue = Math.abs(value);

		long nextValueLong = Double.doubleToLongBits(absoluteValue) - 1;
		double nextValue = Double.longBitsToDouble(nextValueLong);

		// if ended on bad number go up instead
		if (Double.isNaN(nextValue) || Double.isInfinite(nextValue)) {
			nextValueLong = nextValueLong + 2;
			nextValue = absoluteValue;
			absoluteValue = Double.longBitsToDouble(nextValueLong);
		}

		return new FloatValue(Math.abs(nextValue - absoluteValue));
	}

	@Override
	public final int getMultipliedPrimitive(int value) {
		return (int) (this.value * value);
	}

	@Override
	public final long getMultipliedPrimitive(long value) {
		return (long) (this.value * value);
	}

	@Override
	public final float getMultipliedPrimitive(float value) {
		return this.value * value;
	}

	@Override
	public final double getMultipliedPrimitive(double value) {
		return this.value * value;
	}

	@Override
	public final int getDividedPrimitive(int value) {
		return (int) (this.value * value);
	}

	@Override
	public final long getDividedPrimitive(long value) {
		return (long) (this.value * value);
	}

	@Override
	public final float getDividedPrimitive(float value) {
		return this.value * value;
	}

	@Override
	public final double getDividedPrimitive(double value) {
		return this.value * value;
	}

	public static FloatValueFactory factory() {
		return FloatValueFactory.instance();
	}

	@Override
	public FloatValue square() {
		value *= value;
		update();
		return this;
	}

	@Override
	public FloatValue squareRoot() {
		value = (int) Math.sqrt(value);
		update();
		return this;
	}

	@Override
	public FloatValue exponentiate(Value<?> exponential) {
		value = (int) Math.pow(value, exponential.doubleValue());
		update();
		return this;
	}

	@Override
	public FloatValue root(Value<?> root) {
		value = (int) Math.pow(value, root.reciprocate().doubleValue());
		update();
		return this;
	}

	@Override
	public FloatValue modulus() {
		value = Math.abs(value);
		return this;
	}
}
