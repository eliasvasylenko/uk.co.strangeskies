/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix.building.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.building.MatrixBuilder;
import uk.co.strangeskies.mathematics.geometry.matrix.building.ValueMatrixBuilder;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.FloatValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.LongValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

@Component
public class MatrixBuilderImpl implements MatrixBuilder {
	private Order defaultOrder = Order.ColumnMajor;
	private Orientation defaultOrientation = Orientation.Column;

	@Override
	public ValueMatrixBuilder<IntValue> ints() {
		return values(IntValue.factory());
	}

	@Override
	public ValueMatrixBuilder<LongValue> longs() {
		return values(LongValue.factory());
	}

	@Override
	public ValueMatrixBuilder<FloatValue> floats() {
		return values(FloatValue.factory());
	}

	@Override
	public ValueMatrixBuilder<DoubleValue> doubles() {
		return values(DoubleValue.factory());
	}

	@Override
	public <V extends Value<V>> ValueMatrixBuilder<V> values(
			Factory<V> valueFactory) {
		return new ValueMatrixBuilderImpl<>(valueFactory).order(defaultOrder)
				.orientation(defaultOrientation);
	}

	@Override
	public Order getDefaultOrder() {
		return defaultOrder;
	}

	@Override
	public void setDefaultOrder(Order defaultOrder) {
		this.defaultOrder = defaultOrder;
	}

	@Override
	public Orientation getDefaultOrientation() {
		return defaultOrientation;
	}

	@Override
	public void setDefaultOrientation(Orientation defaultOrientation) {
		this.defaultOrientation = defaultOrientation;
	}
}