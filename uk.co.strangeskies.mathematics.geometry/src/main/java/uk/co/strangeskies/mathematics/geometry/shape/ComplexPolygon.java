package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.mathematics.values.Value;

public interface ComplexPolygon<S extends ComplexPolygon<S, V>, V extends Value<V>>
		extends /* @Immutable */CompoundPolygon<S, V> {
	/**
	 * Vertices describing polygon
	 *
	 * Guaranteed to be continuous/unbroken path describing the polygon, but may
	 * include repeated elements where there is self intersection of edges. In the
	 * case of a ComplexPolygon class, for example, this will effectively return a
	 * keyholed and stitched representation of the component contours.
	 *
	 * The path described is <em>not</em> guaranteed to not contain degenerate
	 * edges or self intersections.
	 */
	public/* @ReadOnly */ClosedPolyline2<V> boundary();

	@Override
	public default Set<ClosedPolyline2<V>> boundaryComponents() {
		return new HashSet<>(Arrays.asList(boundary()));
	}

	@Override
	default CompoundPolygon<?, V> nand(CompoundPolygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CompoundPolygon<?, V> nor(CompoundPolygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CompoundPolygon<?, V> or(CompoundPolygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CompoundPolygon<?, V> xnor(CompoundPolygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	default CompoundPolygon<?, V> xor(CompoundPolygon<?, V> value) {
		throw new UnsupportedOperationException();
	}
}
