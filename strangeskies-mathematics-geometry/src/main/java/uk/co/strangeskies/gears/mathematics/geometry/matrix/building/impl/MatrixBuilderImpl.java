package uk.co.strangeskies.gears.mathematics.geometry.matrix.building.impl;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.building.MatrixBuilder;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.building.ValueMatrixBuilder;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.FloatValue;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.LongValue;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

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
