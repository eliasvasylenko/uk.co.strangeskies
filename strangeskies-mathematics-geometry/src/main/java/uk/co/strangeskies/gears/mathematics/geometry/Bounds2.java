package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Bounds2<V extends Value<V>> extends Bounds<Bounds2<V>, V> {
	public Bounds2(Bounds<?, V> other) throws DimensionalityException {
		super(other, 2);
	}

	public Bounds2(Bounds2<V> other) {
		super(other);
	}

	public Bounds2(Vector2Impl<V> from, Vector2Impl<V> to) {
		super(from, to);
	}

	public Bounds2(Vector<?, V> from, Vector<?, V> to) {
		super(from, to, 2);
	}

	public Bounds2(@SuppressWarnings("unchecked") Vector2Impl<V>... points) {
		super(points);
	}

	public Bounds2(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		super(2, points);
	}

	public Bounds2(Collection<? extends Vector<?, V>> points) {
		super(2, points);
	}

	public Bounds2(Factory<V> valueFactory) {
		super(2, valueFactory);
	}

	public final Range<V> getRangeX() {
		return super.getRange(0);
	}

	public final Range<V> getRangeY() {
		return super.getRange(1);
	}

	@Override
	public final Bounds2<V> copy() {
		return new Bounds2<V>(this);
	}
}
