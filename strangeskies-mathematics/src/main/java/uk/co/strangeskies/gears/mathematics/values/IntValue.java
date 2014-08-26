package uk.co.strangeskies.gears.mathematics.values;

public final class IntValue extends IntegralValue<IntValue> {
	private static final long serialVersionUID = 5916143538083079342L;

	private int value;

	public IntValue() {
	}

	public IntValue(Value<?> value) {
		super((Number) value);
	}

	public IntValue(Number value) {
		super(value);
	}

	@Override
	public final IntValue reciprocate() {
		value = 1 / value;

		return this;
	}

	@Override
	public final IntValue add(Value<?> value) {
		this.value += value.intValue();
		update();
		return this;
	}

	@Override
	public final IntValue negate() {
		value = -value;
		update();
		return this;
	}

	@Override
	public final IntValue multiply(int value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final IntValue multiply(long value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final IntValue multiply(float value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final IntValue multiply(double value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final IntValue multiply(Value<?> value) {
		this.value = value.getMultipliedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final IntValue divide(int value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final IntValue divide(long value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final IntValue divide(float value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final IntValue divide(double value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final IntValue divide(Value<?> value) {
		this.value = value.getDividedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final IntValue subtract(Value<?> value) {
		this.value -= value.intValue();
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
		return value;
	}

	@Override
	public final long longValue() {
		return value;
	}

	@Override
	public final String toString() {
		return new Integer(value).toString();
	}

	@Override
	public final IntValue set(Value<?> value) {
		this.value = value.intValue();
		update();
		return this;
	}

	@Override
	public final IntValue setValue(Number value) {
		this.value = value.intValue();
		update();
		return this;
	}

	@Override
	public final int compareToAtSupportedPrecision(Value<?> other) {
		return new Integer(this.value).compareTo(new Integer(other.intValue()));
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
		return new Integer(value).hashCode();
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
	public final IntValue increment() {
		value++;
		update();
		return this;
	}

	@Override
	public final IntValue decrement() {
		value--;
		update();
		return this;
	}

	@Override
	public final IntValue copy() {
		return new IntValue(this);
	}

	@Override
	public final IntValue unitInTheLastPlaceAbove() {
		return new IntValue(1);
	}

	@Override
	public final IntValue unitInTheLastPlaceBelow() {
		return new IntValue(1);
	}

	@Override
	public final int getMultipliedPrimitive(int value) {
		return this.value * value;
	}

	@Override
	public final long getMultipliedPrimitive(long value) {
		return this.value * value;
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
		return this.value * value;
	}

	@Override
	public final long getDividedPrimitive(long value) {
		return this.value * value;
	}

	@Override
	public final float getDividedPrimitive(float value) {
		return this.value * value;
	}

	@Override
	public final double getDividedPrimitive(double value) {
		return this.value * value;
	}

	public static IntValueFactory factory() {
		return IntValueFactory.instance();
	}

	@Override
	public IntValue square() {
		value *= value;
		update();
		return this;
	}

	@Override
	public IntValue squareRoot() {
		value = (int) Math.sqrt(value);
		update();
		return this;
	}

	@Override
	public IntValue exponentiate(Value<?> exponential) {
		value = (int) Math.pow(value, exponential.doubleValue());
		update();
		return this;
	}

	@Override
	public IntValue root(Value<?> root) {
		value = (int) Math.pow(value, root.reciprocate().doubleValue());
		update();
		return this;
	}

	@Override
	public IntValue modulus() {
		value = Math.abs(value);
		return this;
	}
}
