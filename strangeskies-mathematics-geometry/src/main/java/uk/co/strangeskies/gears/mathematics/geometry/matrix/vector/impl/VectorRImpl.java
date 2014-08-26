package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorR;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class VectorRImpl<V extends Value<V>> extends VectorImpl<VectorR<V>, V>
		implements VectorR<V> {
	public VectorRImpl(int size, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(size, order, orientation, valueFactory);
	}

	public VectorRImpl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);
	}

	@Override
	public VectorR<V> resize(int dimensions) {
		return super.resizeImplementation(dimensions);
	}

	@Override
	public VectorR<V> getResized(int dimensions) {
		return copy().resize(dimensions);
	}

	@Override
	public VectorR<V> copy() {
		return new VectorRImpl<>(getOrder(), getOrientation(), getData());
	}
}
