/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values;

// TODO put all the update() read() lock protection stuff in the other Value implementations
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
		return update(() -> value = 1 / value);
	}

	@Override
	public final LongValue add(Value<?> value) {
		return update(() -> this.value += value.longValue());
	}

	@Override
	public final LongValue negate() {
		return update(() -> value = -value);
	}

	@Override
	public final LongValue multiply(int value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final LongValue multiply(long value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final LongValue multiply(float value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final LongValue multiply(double value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final LongValue multiply(Value<?> value) {
		return update(() -> this.value = value.getMultipliedPrimitive(this.value));
	}

	@Override
	public final LongValue divide(int value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final LongValue divide(long value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final LongValue divide(float value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final LongValue divide(double value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final LongValue divide(Value<?> value) {
		return update(() -> this.value = value.getDividedPrimitive(this.value));
	}

	@Override
	public final LongValue subtract(Value<?> value) {
		return update(() -> this.value -= value.longValue());
	}

	@Override
	public final double doubleValue() {
		return read(() -> (double) value);
	}

	@Override
	public final float floatValue() {
		return read(() -> (float) value);
	}

	@Override
	public final int intValue() {
		return read(() -> (int) value);
	}

	@Override
	public final long longValue() {
		return read(() -> value);
	}

	@Override
	public final String toString() {
		return read(() -> value).toString();
	}

	@Override
	public final LongValue set(Value<?> value) {
		return update(() -> this.value = value.longValue());
	}

	@Override
	public final LongValue setValue(Number value) {
		return update(() -> this.value = value.longValue());
	}

	@Override
	public final int compareToAtSupportedPrecision(Value<?> other) {
		return read(() -> this.value).compareTo(new Long(other.longValue()));
	}

	@Override
	public final boolean equals(Object that) {
		return read(() -> {
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
		});
	}

	@Override
	protected final boolean equals(Value<?> that) {
		return read(() -> ((Value<?>) that).equals(this.value));
	}

	@Override
	public final int hashCode() {
		return read(() -> new Long(value).hashCode());
	}

	@Override
	public final boolean equals(double value) {
		return read(() -> this.value == value);
	}

	@Override
	public final boolean equals(float value) {
		return read(() -> this.value == value);
	}

	@Override
	public final boolean equals(int value) {
		return read(() -> this.value == value);
	}

	@Override
	public final boolean equals(long value) {
		return read(() -> this.value == value);
	}

	@Override
	public final LongValue increment() {
		return read(() -> update(() -> value++));
	}

	@Override
	public final LongValue decrement() {
		return read(() -> update(() -> value--));
	}

	@Override
	public final LongValue copy() {
		return read(() -> new LongValue(this));
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
		return read(() -> (int) (this.value * value));
	}

	@Override
	public final long getMultipliedPrimitive(long value) {
		return read(() -> this.value * value);
	}

	@Override
	public final float getMultipliedPrimitive(float value) {
		return read(() -> this.value * value);
	}

	@Override
	public final double getMultipliedPrimitive(double value) {
		return read(() -> this.value * value);
	}

	@Override
	public final int getDividedPrimitive(int value) {
		return read(() -> (int) (this.value * value));
	}

	@Override
	public final long getDividedPrimitive(long value) {
		return read(() -> this.value * value);
	}

	@Override
	public final float getDividedPrimitive(float value) {
		return read(() -> this.value * value);
	}

	@Override
	public final double getDividedPrimitive(double value) {
		return read(() -> this.value * value);
	}

	public static LongValueFactory factory() {
		return LongValueFactory.instance();
	}

	@Override
	public LongValue square() {
		return update(() -> value *= value);
	}

	@Override
	public LongValue squareRoot() {
		return update(() -> value = (int) Math.sqrt(value));
	}

	@Override
	public LongValue exponentiate(Value<?> exponential) {
		return update(() -> value = (int) Math
				.pow(value, exponential.doubleValue()));
	}

	@Override
	public LongValue root(Value<?> root) {
		return update(() -> value = (int) Math.pow(value, root.reciprocate()
				.doubleValue()));
	}

	@Override
	public LongValue modulus() {
		return update(() -> value = Math.abs(value));
	}
}
