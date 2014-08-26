package uk.co.strangeskies.gears.mathematics.values;

public class LongValue extends IntegralValue<LongValue> {
	private static final long serialVersionUID = 5916143538083079342L;

	private long value;

	public LongValue() {
	}

	public LongValue(Value<?> value) {
		super((Number) value);
	}

	public LongValue(Number value) {
		super(value);
	}

	@Override
	public final LongValue reciprocate() {
		value = 1 / value;

		return this;
	}

	@Override
	public final LongValue add(Value<?> value) {
		this.value += value.longValue();
		update();
		return this;
	}

	@Override
	public final LongValue negate() {
		value = -value;
		update();
		return this;
	}

	@Override
	public final LongValue multiply(int value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final LongValue multiply(long value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final LongValue multiply(float value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final LongValue multiply(double value) {
		this.value *= value;
		update();
		return this;
	}

	@Override
	public final LongValue multiply(Value<?> value) {
		this.value = value.getMultipliedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final LongValue divide(int value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final LongValue divide(long value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final LongValue divide(float value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final LongValue divide(double value) {
		this.value /= value;
		update();
		return this;
	}

	@Override
	public final LongValue divide(Value<?> value) {
		this.value = value.getDividedPrimitive(this.value);
		update();
		return this;
	}

	@Override
	public final LongValue subtract(Value<?> value) {
		this.value -= value.longValue();
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
		return value;
	}

	@Override
	public final String toString() {
		return new Long(value).toString();
	}

	@Override
	public final LongValue set(Value<?> value) {
		this.value = value.longValue();
		update();
		return this;
	}

	@Override
	public final LongValue setValue(Number value) {
		this.value = value.longValue();
		update();
		return this;
	}

	@Override
	public final int compareToAtSupportedPrecision(Value<?> other) {
		return new Long(this.value).compareTo(new Long(other.longValue()));
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
		return new Long(value).hashCode();
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
	public final LongValue increment() {
		value++;
		update();
		return this;
	}

	@Override
	public final LongValue decrement() {
		value--;
		update();
		return this;
	}

	@Override
	public final LongValue copy() {
		return new LongValue(this);
	}

	@Override
	public final LongValue unitInTheLastPlaceAbove() {
		return new LongValue(1);
	}

	@Override
	public final LongValue unitInTheLastPlaceBelow() {
		return new LongValue(1);
	}

	@Override
	public final int getMultipliedPrimitive(int value) {
		return (int) (this.value * value);
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
		return (int) (this.value * value);
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

	public static LongValueFactory factory() {
		return LongValueFactory.instance();
	}

	@Override
	public LongValue square() {
		value *= value;
		update();
		return this;
	}

	@Override
	public LongValue squareRoot() {
		value = (int) Math.sqrt(value);
		update();
		return this;
	}

	@Override
	public LongValue exponentiate(Value<?> exponential) {
		value = (int) Math.pow(value, exponential.doubleValue());
		update();
		return this;
	}

	@Override
	public LongValue root(Value<?> root) {
		value = (int) Math.pow(value, root.reciprocate().doubleValue());
		update();
		return this;
	}

	@Override
	public LongValue modulus() {
		value = Math.abs(value);
		return this;
	}
}
