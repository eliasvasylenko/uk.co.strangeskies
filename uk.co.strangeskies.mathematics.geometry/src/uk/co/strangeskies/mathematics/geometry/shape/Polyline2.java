package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.PolylineN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;

public interface Polyline2<S extends Polyline2<S, V>, V extends Value<V>>
		extends Shape<S>, PolylineN<Vector2<V>, V> {
	@Override
	public Bounds2<V> getBounds();

	@Override
	public List<Line2<V>> lines();

	@Override
	default Value<?> getArea() {
		return new IntValue();
	}

	@Override
	default Value<?> getPerimeter() {
		return getLength().multiply(2);
	}

	@Override
	default boolean contains(Vector2<?> point) {
		return false;
	}

	@Override
	default boolean touches(Vector2<?> point) {
		return lines().stream().anyMatch(l -> l.touches(point));
	}
}
