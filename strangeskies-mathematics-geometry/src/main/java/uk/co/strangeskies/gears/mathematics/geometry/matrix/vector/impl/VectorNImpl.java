package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class VectorNImpl<V extends Value<V>> extends VectorImpl<VectorN<V>, V>
		implements VectorN<V> {
	public VectorNImpl(int size, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(size, order, orientation, valueFactory);
	}

	public VectorNImpl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);
	}

	@Override
	public VectorN<V> copy() {
		return new VectorNImpl<V>(getOrder(), getOrientation(), getData());
	}
}
