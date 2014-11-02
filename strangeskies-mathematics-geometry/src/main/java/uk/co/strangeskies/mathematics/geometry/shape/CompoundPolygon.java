package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.Set;

import uk.co.strangeskies.mathematics.logic.BooleanCombinationBehaviour;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 *
 * @author eli
 *
 * @param <V>
 */
public interface CompoundPolygon<V extends Value<V>> extends
		Self<CompoundPolygon<V>>, ComplexPolygon<CompoundPolygon<V>, V>,
		/* @Mutable */
		BooleanCombinationBehaviour<CompoundPolygon<V>, Polygon<?, V>> {
	/**
	 * Return set of component polygons. These polygons may be self intersecting
	 * and may intersect each other. There are no
	 *
	 * @return
	 */
	public Set<Polygon<?, V>> getComponentSet();
}
