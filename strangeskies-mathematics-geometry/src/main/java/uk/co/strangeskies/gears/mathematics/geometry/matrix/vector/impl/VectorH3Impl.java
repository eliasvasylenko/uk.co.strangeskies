package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class VectorH3Impl<V extends Value<V>> extends
		VectorHImpl<VectorH3<V>, V> implements VectorH3<V> {
	public VectorH3Impl(Type type, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(type, 3, order, orientation, valueFactory);
	}

	public VectorH3Impl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(type, order, orientation, values);
	}

	@Override
	protected void finalise() {
		super.finalise();

		assertDimensions(this, 4);
	}

	@Override
	public Vector2<V> getMutableVector() {
		Vector2<V> mutableVector = new Vector2Impl<V>(getOrder(), getOrientation(),
				getData().subList(0, 3));

		return mutableVector;
	}

	@Override
	public VectorH3<V> copy() {
		return new VectorH3Impl<>(getType(), getOrder(), getOrientation(),
				getData());
	}

	@Override
	public V getX() {
		return getElement(0);
	}

	@Override
	public V getY() {
		return getElement(1);
	}

	@Override
	public V getW() {
		return getElement(2);
	}
}
