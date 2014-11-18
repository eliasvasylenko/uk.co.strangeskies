package uk.co.strangeskies.mathematics.geometry;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public interface PolylineN<T extends Vector<T, V>, V extends Value<V>> {
	public List<? extends LineN<T, V>> lines();

	public List<T> vertices();

	public Bounds<?, V> getBounds();

	public Value<?> getLength();

	default boolean touches(Vector2<?> point) {
		return lines().stream().anyMatch(l -> l.touches(point));
	}
}
