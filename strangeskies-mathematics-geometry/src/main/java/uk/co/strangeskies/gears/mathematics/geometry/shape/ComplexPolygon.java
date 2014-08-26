package uk.co.strangeskies.gears.mathematics.geometry.shape;

import uk.co.strangeskies.gears.mathematics.values.Value;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 * 
 * @author eli
 * 
 * @param <V>
 */
public interface ComplexPolygon<S extends ComplexPolygon<S, V>, V extends Value<V>>
		extends Polygon<S, V> {
	enum Winding {
		Odd, NonZero, CW, CCW
	}
}
