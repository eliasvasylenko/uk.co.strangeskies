package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class BoundsN<V extends Value<V>> extends Bounds<BoundsN<V>, V> {
	public BoundsN(Bounds<?, V> other) {
		super(other);
	}

	public BoundsN(Vector<?, V> from, Vector<?, V> to) {
		super(from, to);
	}

	public BoundsN(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		super(points);
	}

	public BoundsN(Collection<? extends Vector<?, V>> points) {
		super(points);
	}

	public BoundsN(int size, Factory<V> valueFactory) {
		super(size, valueFactory);
	}

	@Override
	public final BoundsN<V> copy() {
		return new BoundsN<V>(this);
	}
}
