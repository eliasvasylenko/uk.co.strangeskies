package uk.co.strangeskies.gears.mathematics.geometry.shape;

import java.util.Set;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;

/**
 * 
 * @author eli
 * 
 * @param <V>
 */
public interface ConvexPolygon<V extends Value<V>> extends
		SimplePolygon<ConvexPolygon<V>, V> {
	/**
	 * This method returns a modifiable set containing all vertices which
	 * currently effectively make a contribution to the convex Hull of this
	 * Polygon, i.e. all vertices which form a part of the outside edge and are
	 * returned by the {@link ConvexPolygon#getVertices()}.
	 * 
	 * The Set is backed by the shape and changes will be reflected in it. If
	 * vertices are moved around such that the convex hull changes any set
	 * previously returned will reflect this change.
	 * 
	 * Adding vertices to the set will succeed if they are not already present,
	 * and fall outside the current perimeter of the shape, and the shape will be
	 * updated to reflect the addition. If the vertices being added lie within the
	 * perimeter the addition to the set will fail and the underlying model will
	 * not be changed. If more than one vertex is added at the same time, only
	 * those which remain a part of this set after they have all been added will
	 * remain a part of the underlying model.
	 * 
	 * @return
	 */
	public Set<Vector2<V>> hullVertexSet();

	/**
	 * This method returns a modifiable set representing the underlying collection
	 * of vertices from which the convex hull represented by this polygon is
	 * generated. This set may contain vertices which lie within the convex hull,
	 * such that on removal of other vertices, or movement of vertices, they may
	 * become a part of the convex hull.
	 * 
	 * The Set is backed by the shape and changes will be reflected in it. If
	 * vertices are added or removed from the shape, they will be added to or
	 * removed from this set.
	 * 
	 * Adding vertices to the set will always succeed if they are not already
	 * present.
	 * 
	 * @return
	 */
	public Set<Vector2<V>> vertexSet();
}
