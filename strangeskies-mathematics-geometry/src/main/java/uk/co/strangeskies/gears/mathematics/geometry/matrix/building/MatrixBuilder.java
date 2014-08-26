package uk.co.strangeskies.gears.mathematics.geometry.matrix.building;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.FloatValue;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.LongValue;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public interface MatrixBuilder {
	public Order getDefaultOrder();

	public void setDefaultOrder(Order defaultOrder);

	public Orientation getDefaultOrientation();

	public void setDefaultOrientation(Orientation defaultOrientation);

	public ValueMatrixBuilder<IntValue> ints();

	public ValueMatrixBuilder<LongValue> longs();

	public ValueMatrixBuilder<FloatValue> floats();

	public ValueMatrixBuilder<DoubleValue> doubles();

	public <V extends Value<V>> ValueMatrixBuilder<V> values(
			Factory<V> valueFactory);
}
