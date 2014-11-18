package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.LineN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public interface Line2<V extends Value<V>> extends Shape<Line2<V>>,
		LineN<Vector2<V>, V> {
	@Override
	public Bounds2<V> getBounds();

	@Override
	default boolean touches(Vector2<?> point) {
		return touches((Vector<?, ?>) point);
	}
}
