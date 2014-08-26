package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Vector2Impl<V extends Value<V>> extends VectorImpl<Vector2<V>, V>
		implements Vector2<V> {
	public Vector2Impl(Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(2, order, orientation, valueFactory);
	}

	public Vector2Impl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);

		assertDimensions(this, 2);
	}

	@Override
	public final Vector2<V> copy() {
		return new Vector2Impl<V>(getOrder(), getOrientation(), getData());
	}

	@Override
	public final V getX() {
		return getElement(0);
	}

	@Override
	public final V getY() {
		return getElement(1);
	}

	@Override
	public final Vector2<V> getRotated(Value<?> angle) {
		return copy().rotate(angle);
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle) {
		// TODO implement rotation
		return null;
	}

	@Override
	public final Vector2<V> getRotated(Value<?> angle, Vector2<?> centre) {
		return copy().rotate(angle, centre);
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement rotation about point
		return null;
	}

	@Override
	public Vector2<V> setData(Dimension dimension) {
		return setData(dimension.getWidth(), dimension.getHeight());
	}

	@Override
	public Vector2<V> setData(Point point) {
		return setData(point.getX(), point.getY());
	}

	@Override
	public Vector2<V> setData(Point2D point) {
		return setData(point.getX(), point.getY());
	}

	@Override
	public Dimension getDimension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point2D getPoint2D() {
		// TODO Auto-generated method stub
		return null;
	}
}
