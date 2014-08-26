package uk.co.strangeskies.gears.mathematics.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.Self;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public abstract class Bounds<S extends Bounds<S, V>, V extends Value<V>>
		implements Self<S> {
	ArrayList<Range<V>> ranges;

	public Bounds(int dimensions, Factory<V> valueFactory) {
		try {
			DimensionalityException.checkValid(dimensions);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		ranges = new ArrayList<Range<V>>();
		for (int i = 0; i < dimensions; i++) {
			ranges.add(Range.create(valueFactory.create(), valueFactory.create()));
		}
	}

	public Bounds(Vector<?, V> from, Vector<?, V> to) {
		this(from, to, from.getDimensions());
	}

	public Bounds(Vector<?, V> from, Vector<?, V> to, int dimensions) {
		try {
			DimensionalityException
					.checkEquivalence(from.getDimensions(), dimensions);
			DimensionalityException.checkEquivalence(to.getDimensions(), dimensions);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}
		ranges = new ArrayList<Range<V>>();

		Iterator<? extends V> fromIterator = from.getData().iterator();
		Iterator<? extends V> toIterator = to.getData().iterator();
		while (fromIterator.hasNext()) {
			ranges.add(Range.create(fromIterator.next(), toIterator.next()));
		}
	}

	public Bounds(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		this(Arrays.asList(points));
	}

	public Bounds(int dimensions,
			@SuppressWarnings("unchecked") Vector<?, V>... points) {
		this(dimensions, Arrays.asList(points));
	}

	public Bounds(Collection<? extends Vector<?, V>> points) {
		this(points.iterator().next().getDimensions(), points);
	}

	public Bounds(int dimensions, Collection<? extends Vector<?, V>> points) {
		try {
			for (Vector<?, V> point : points) {
				DimensionalityException.checkEquivalence(dimensions,
						point.getDimensions());
			}
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}
		ranges = new ArrayList<Range<V>>();

		Iterator<? extends Vector<?, V>> pointIterator = points.iterator();
		Vector<?, V> firstPoint = pointIterator.next();
		for (V value : firstPoint.getData()) {
			ranges.add(Range.create(value, value));
		}
		while (pointIterator.hasNext()) {
			Iterator<Range<V>> rangeIterator = ranges.iterator();
			for (V value : pointIterator.next().getData()) {
				rangeIterator.next().extendThrough(value, true);
			}
		}
	}

	public Bounds(Bounds<?, V> other) {
		ranges = new ArrayList<Range<V>>();
		for (Range<V> range : other.getData()) {
			ranges.add(range.copy());
		}
	}

	public Bounds(Bounds<?, V> other, int dimensions) {
		try {
			DimensionalityException
					.checkEquivalence(other.getDimension(), dimensions);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}
		ranges = new ArrayList<Range<V>>();
		for (Range<V> range : other.getData()) {
			ranges.add(range.copy());
		}
	}

	public final List<Range<V>> getData() {
		return Collections.unmodifiableList(ranges);
	}

	public final Range<V> getRange(int index) {
		return getData().get(index);
	}

	public int getDimension() {
		return ranges.size();
	}

	public void expandThrough(
			@SuppressWarnings("unchecked") Vector<?, V>... points) {
		expandThrough(Arrays.asList(points));
	}

	public void expandThrough(Collection<? extends Vector<?, V>> points) {
		for (Vector<?, V> point : points) {
			Iterator<Range<V>> rangeIterator = ranges.iterator();
			for (V value : point.getData()) {
				rangeIterator.next().extendThrough(value, true);
			}
		}
	}

	public <M extends Vector<M, X>, X extends Value<X>> M getConfined(M value) {
		M confined = value.copy();
		return this.confine(confined);
	}

	public <M extends Vector<M, X>, X extends Value<X>> M confine(M value) {
		try {
			DimensionalityException.checkEquivalence(getDimension(),
					value.getDimensions());
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}
		Iterator<X> valueIterator = value.getData().iterator();
		for (Range<V> range : ranges) {
			range.confineComparable(valueIterator.next());
		}
		return value;
	}

	public S getCombinedWith(Bounds<?, V> other) throws DimensionalityException {
		return copy().combineWith(other);
	}

	public S combineWith(Bounds<?, V> other) {
		try {
			DimensionalityException.checkEquivalence(getDimension(),
					other.getDimension());
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}
		Iterator<? extends Range<V>> otherIterator = other.getData().iterator();
		for (Range<V> range : ranges) {
			range.extendThrough(otherIterator.next());
		}
		return getThis();
	}
}
