/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values;

public final class FloatValue extends ContinuousValue<FloatValue> {
	private static final long serialVersionUID = 5916143538083079342L;

	private float value;

	public FloatValue() {}

	public FloatValue(Value<?> value) {
		super(value);
	}

	public FloatValue(Number value) {
		super(value);
	}

	@Override
	public final FloatValue reciprocate() {
		return update(() -> value = 1 / value);
	}

	@Override
	public final FloatValue add(Value<?> value) {
		return update(() -> this.value += value.floatValue());
	}

	@Override
	public final FloatValue negate() {
		return update(() -> value = -value);
	}

	@Override
	public final FloatValue multiply(int value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final FloatValue multiply(long value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final FloatValue multiply(float value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final FloatValue multiply(double value) {
		return update(() -> this.value *= value);
	}

	@Override
	public final FloatValue multiply(Value<?> value) {
		return update(() -> this.value = value.getMultipliedPrimitive(this.value));
	}

	@Override
	public final FloatValue divide(int value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final FloatValue divide(long value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final FloatValue divide(float value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final FloatValue divide(double value) {
		return update(() -> this.value /= value);
	}

	@Override
	public final FloatValue divide(Value<?> value) {
		return update(() -> this.value = value.getDividedPrimitive(this.value));
	}

	@Override
	public final FloatValue subtract(Value<?> value) {
		return update(() -> this.value -= value.floatValue());
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
		return new Float(value).toString();
	}

	@Override
	public final FloatValue set(Value<?> value) {
		return update(() -> this.value = value.floatValue());
	}

	@Override
	public final FloatValue setValue(Number value) {
		return update(() -> this.value = value.floatValue());
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
		return update(() -> value++);
	}

	@Override
	public final FloatValue decrement() {
		return update(() -> value--);
	}

	@Override
	public final FloatValue copy() {
		return new FloatValue(this);
	}

	@Override
	public final FloatValue unitInTheLastPlaceAbove() {
		return new FloatValue(1);
	}

	@Override
	public final FloatValue unitInTheLastPlaceBelow() {
		return new FloatValue(1);
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

	@Override
	public FloatValue square() {
		return update(() -> value *= value);
	}

	@Override
	public FloatValue squareRoot() {
		return update(() -> value = (int) Math.sqrt(value));
	}

	@Override
	public FloatValue exponentiate(Value<?> exponential) {
		return update(
				() -> value = (int) Math.pow(value, exponential.doubleValue()));
	}

	@Override
	public FloatValue root(Value<?> root) {
		return exponentiate(root.getReciprocal());
	}

	@Override
	public FloatValue modulus() {
		return update(() -> value = Math.abs(value));
	}
}
