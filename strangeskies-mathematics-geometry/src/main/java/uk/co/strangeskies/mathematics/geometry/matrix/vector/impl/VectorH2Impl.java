package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class VectorH2Impl<V extends Value<V>> extends
		VectorHImpl<VectorH2<V>, V> implements VectorH2<V> {
	public VectorH2Impl(Type type, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(type, 2, order, orientation, valueFactory);
	}

	public VectorH2Impl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(type, order, orientation, values);

		assertDimensions(this, 3);
	}

	@Override
	public VectorH2<V> copy() {
		return new VectorH2Impl<>(getType(), getOrder(), getOrientation(),
				getData());
	}

	@Override
	public VectorH2<V> rotate(Value<?> angle) {
		getMutableVector().rotate(angle);

		return getThis();
	}

	@Override
	public VectorH2<V> rotate(Value<?> angle, Vector2<?> centre) {
		getMutableVector().rotate(angle, centre);

		return getThis();
	}
}
