package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import uk.co.strangeskies.mathematics.geometry.Rotatable2;
import uk.co.strangeskies.mathematics.values.Value;

public interface Vector2<V extends Value<V>> extends Vector<Vector2<V>, V>,
		Rotatable2<Vector2<V>> {
	public default V getX() {
		return getElement(0);
	}

	public default V getY() {
		return getElement(1);
	}

	public default Vector2<V> setData(Dimension dimension) {
		return setData(dimension.getWidth(), dimension.getHeight());
	}

	public default Vector2<V> setData(Point point) {
		return setData(point.getX(), point.getY());
	}

	public default Vector2<V> setData(Point2D point) {
		return setData(point.getX(), point.getY());
	}

	public default Dimension getDimension() {
		return new Dimension(getX().intValue(), getY().intValue());
	}

	public default Point getPoint() {
		return new Point(getX().intValue(), getY().intValue());
	}

	public default Point2D getPoint2D() {
		return new Point2D.Double(getX().doubleValue(), getY().doubleValue());
	}
}
