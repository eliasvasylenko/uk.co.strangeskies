package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorHN;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class VectorHNImpl<V extends Value<V>> extends
		VectorHImpl<VectorHN<V>, V> implements VectorHN<V> {
	public VectorHNImpl(Type type, int size, Order order,
			Orientation orientation, Factory<V> valueFactory) {
		super(type, size, order, orientation, valueFactory);
	}

	public VectorHNImpl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(type, order, orientation, values);
	}

	@Override
	public VectorNImpl<V> getMutableVector() {
		VectorNImpl<V> mutableVector = new VectorNImpl<>(getOrder(),
				getOrientation(), getData().subList(0, getProjectedDimensions()));

		return mutableVector;
	}

	@Override
	public VectorHN<V> copy() {
		return new VectorHNImpl<>(getType(), getOrder(), getOrientation(),
				getData());
	}
}
