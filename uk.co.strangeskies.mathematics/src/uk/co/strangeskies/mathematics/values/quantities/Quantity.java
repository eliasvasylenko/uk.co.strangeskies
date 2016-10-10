/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values.quantities;

import uk.co.strangeskies.mathematics.values.Value;

public final class Quantity<V extends Value<V>> extends Value<Quantity<V>> {
	private static final long serialVersionUID = 1L;

	private V value;

	private final Unit unit;

	public Quantity(Unit unit) {
		super();
		this.unit = unit;
	}

	public Quantity(Value<?> value, Unit unit) {
		super((Number) value);
		this.unit = unit;
	}

	public Quantity(Number value, Unit unit) {
		super(value);
		this.unit = unit;
	}

	@Override
	public final boolean equals(double value) {
		return this.value.equals(value);
	}

	@Override
	public final boolean equals(float value) {
		return this.value.equals(value);
	}

	@Override
	public final boolean equals(int value) {
		return this.value.equals(value);
	}

	@Override
	public final boolean equals(long value) {
		return this.value.equals(value);
	}

	@Override
	public final int compareToAtSupportedPrecision(Value<?> other) {
		return value.compareToAtSupportedPrecision(other);
	}

	@Override
	public final int getMultipliedPrimitive(int value) {
		return this.value.getMultipliedPrimitive(value);
	}

	@Override
	public final long getMultipliedPrimitive(long value) {
		return this.value.getMultipliedPrimitive(value);
	}

	@Override
	public final float getMultipliedPrimitive(float value) {
		return this.value.getMultipliedPrimitive(value);
	}

	@Override
	public final double getMultipliedPrimitive(double value) {
		return this.value.getMultipliedPrimitive(value);
	}

	@Override
	public final int getDividedPrimitive(int value) {
		return this.value.getDividedPrimitive(value);
	}

	@Override
	public final long getDividedPrimitive(long value) {
		return this.value.getDividedPrimitive(value);
	}

	@Override
	public final float getDividedPrimitive(float value) {
		return this.value.getDividedPrimitive(value);
	}

	@Override
	public final double getDividedPrimitive(double value) {
		return this.value.getDividedPrimitive(value);
	}

	@Override
	public final Quantity<V> unitInTheLastPlaceAbove() {
		return copy().set(unitInTheLastPlaceAbove());
	}

	@Override
	public final Quantity<V> unitInTheLastPlaceBelow() {
		return copy().set(unitInTheLastPlaceBelow());
	}

	@Override
	public final Quantity<V> copy() {
		return new Quantity<V>(this, this.unit);
	}

	public final Quantity<V> multiply(Quantity<V> value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> add(Value<?> value) {
		this.value.add(value);

		return this;
	}

	@Override
	public final Quantity<V> subtract(Value<?> value) {
		this.value.subtract(value);

		return this;
	}

	@Override
	public final Quantity<V> multiply(Value<?> value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> multiply(int value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> multiply(long value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> multiply(float value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> multiply(double value) {
		this.value.multiply(value);

		return this;
	}

	@Override
	public final Quantity<V> divide(Value<?> value) {
		this.value.divide(value);

		return this;
	}

	@Override
	public final Quantity<V> divide(int value) {
		this.value.divide(value);

		return this;
	}

	@Override
	public final Quantity<V> divide(long value) {
		this.value.divide(value);

		return this;
	}

	@Override
	public final Quantity<V> divide(float value) {
		this.value.divide(value);

		return this;
	}

	@Override
	public final Quantity<V> divide(double value) {
		this.value.divide(value);

		return this;
	}

	@Override
	public final Quantity<V> increment() {
		this.value.increment();

		return this;
	}

	@Override
	public final Quantity<V> decrement() {
		this.value.decrement();

		return this;
	}

	@Override
	public final Quantity<V> negate() {
		this.value.negate();

		return this;
	}

	@Override
	public final Quantity<V> reciprocate() {
		this.value.reciprocate();

		return this;
	}

	@Override
	public final String toString() {
		return "(" + value.toString() + " " + unit.toString() + ")";
	}

	@Override
	public final Quantity<V> set(Value<?> value) {
		this.value.set(value);

		return this;
	}

	@Override
	public final Quantity<V> setValue(Number value) {
		this.value.setValue(value);

		return this;
	}

	@Override
	public final boolean equals(Object that) {
		return this.value.equals(value);
	}

	@Override
	protected final boolean equals(Value<?> that) {
		return this.value.equals(value);
	}

	@Override
	public final double doubleValue() {
		return this.value.doubleValue();
	}

	@Override
	public final float floatValue() {
		return this.value.floatValue();
	}

	@Override
	public final int intValue() {
		return this.value.intValue();
	}

	@Override
	public final long longValue() {
		return this.value.longValue();
	}

	@Override
	public Quantity<V> square() {
		value.square();

		return this;
	}

	@Override
	public Quantity<V> squareRoot() {
		value.squareRoot();

		return this;
	}

	@Override
	public Quantity<V> exponentiate(Value<?> exponential) {
		value.exponentiate(exponential);

		return this;
	}

	@Override
	public Quantity<V> root(Value<?> root) {
		value.root(root);

		return this;
	}

	@Override
	public Quantity<V> modulus() {
		value.modulus();

		return this;
	}
}
