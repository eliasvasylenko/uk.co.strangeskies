package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Bounds3<V extends Value<V>> extends Bounds<Bounds3<V>, V> {
	public Bounds3(Bounds<?, V> other) {
		super(other, 3);
	}

	public Bounds3(Bounds3<V> other) {
		super(other);
	}

	public Bounds3(Vector<?, V> from, Vector<?, V> to) {
		super(from, to, 3);
	}

	public Bounds3(Vector3Impl<V> from, Vector3Impl<V> to) {
		super(from, to);
	}

	public Bounds3(@SuppressWarnings("unchecked") Vector3Impl<V>... points) {
		super(points);
	}

	public Bounds3(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		super(3, points);
	}

	public Bounds3(Collection<? extends Vector<?, V>> points) {
		super(3, points);
	}

	public Bounds3(Factory<V> valueFactory) {
		super(3, valueFactory);
	}

	public final Range<V> getRangeX() {
		return super.getRange(0);
	}

	public final Range<V> getRangeY() {
		return super.getRange(1);
	}

	public final Range<V> getRangeZ() {
		return super.getRange(2);
	}

	@Override
	public final Bounds3<V> copy() {
		return new Bounds3<V>(this);
	}
}
