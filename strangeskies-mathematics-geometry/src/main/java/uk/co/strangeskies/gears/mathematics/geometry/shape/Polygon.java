package uk.co.strangeskies.gears.mathematics.geometry.shape;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.logic.BooleanCombinationBehaviour;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Polygon<S extends Polygon<S, V>, V extends Value<V>> extends
		Shape<S>, /* @ReadOnly */
		BooleanCombinationBehaviour<CompoundPolygon<V>, Polygon<?, V>> {
	/**
	 * Gets the area where anti-clockwise wound polygons are negative.
	 * 
	 * @return
	 */
	@Override
	public Value<?> getArea();

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
	public/* @ReadOnly */List<Vector2<V>> getVertices()/* @ReadOnly */;
}
