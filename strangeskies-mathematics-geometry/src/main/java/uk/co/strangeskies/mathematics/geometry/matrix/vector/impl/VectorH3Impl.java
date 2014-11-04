package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class VectorH3Impl<V extends Value<V>> extends
		VectorHImpl<VectorH3<V>, V> implements VectorH3<V> {
	public VectorH3Impl(Type type, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(type, 3, order, orientation, valueFactory);
	}

	public VectorH3Impl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(type, order, orientation, values);

		assertDimensions(this, 4);
	}

	@Override
	public VectorH3<V> copy() {
		return new VectorH3Impl<>(getType(), getOrder(), getOrientation(),
				getData());
	}

	@Override
	public Vector3<V> getMutableVector() {
		Vector3<V> mutableVector = new Vector3Impl<V>(getOrder(), getOrientation(),
				getData().subList(0, 3));

		return mutableVector;
	}
}
