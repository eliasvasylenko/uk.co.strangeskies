package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import uk.co.strangeskies.gears.mathematics.geometry.Rotatable2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Vector2<V extends Value<V>> extends Vector<Vector2<V>, V>,
		Rotatable2<Vector2<V>> {
	public V getX();

	public V getY();

	public Vector2<V> setData(Dimension dimension);

	public Vector2<V> setData(Point point);

	public Vector2<V> setData(Point2D point);

	public Dimension getDimension();

	public Point getPoint();

	public Point2D getPoint2D();
}
