package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.geometry.ClosedPolylineN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public interface ClosedPolyline2<V extends Value<V>> extends
		Shape<ClosedPolyline2<V>>, Polyline2<ClosedPolyline2<V>, V>,
		ClosedPolylineN<Vector2<V>, V> {
}
