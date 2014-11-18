package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class Vector2Impl<V extends Value<V>> extends VectorImpl<Vector2<V>, V>
		implements Vector2<V> {
	public Vector2Impl(Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(2, order, orientation, valueFactory);
	}

	public Vector2Impl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);

		assertDimensions(this, 2);
	}

	@Override
	public final Vector2<V> copy() {
		return new Vector2Impl<V>(getOrder(), getOrientation(), getData());
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle) {
		// TODO implement rotation
		return null;
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement rotation about point
		return null;
	}
}
