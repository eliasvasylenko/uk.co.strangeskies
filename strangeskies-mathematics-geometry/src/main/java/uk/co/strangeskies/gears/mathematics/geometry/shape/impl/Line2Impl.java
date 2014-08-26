package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import uk.co.strangeskies.gears.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.geometry.Bounds2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Line2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Line2Impl<V extends Value<V>> extends CompoundExpression<Line2<V>>
		implements Line2<V>, CopyDecouplingExpression<Line2<V>> {
	private final Vector2<V> a;
	private final Vector2<V> b;

	public Line2Impl(Factory<V> valueFactory) {
		a = new Vector2Impl<>(Order.ColumnMajor, Orientation.Column, valueFactory);
		b = new Vector2Impl<>(Order.ColumnMajor, Orientation.Column, valueFactory);
	}

	public Line2Impl(Vector2<V> a, Vector2<V> b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public IntValue getArea() {
		return new IntValue();
	}

	@Override
	public DoubleValue getPerimeter() {
		return getLength().multiply(2);
	}

	@Override
	public DoubleValue getLength() {
		return a.getSubtracted(b).getSize();
	}

	@Override
	public V getLengthSquared() {
		return a.getSubtracted(b).getSizeSquared();
	}

	@Override
	public Bounds2<V> getBounds() {
		return new Bounds2<>(a, b);
	}

	@Override
	public Line2<V> copy() {
		return new Line2Impl<>(a, b);
	}

	@Override
	public boolean contains(Vector2<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Vector2<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(Shape<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector2<V> getA() {
		return a;
	}

	@Override
	public Vector2<V> getB() {
		return b;
	}

	@Override
	public Vector2<V> getAB() {
		return b.getSubtracted(a);
	}

	@Override
	public Vector2<V> getBA() {
		return a.getSubtracted(b);
	}

	@Override
	public Line2<V> set(Line2<V> to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Line2<V> get() {
		return getThis();
	}

	@Override
	protected Line2<V> evaluate() {
		return getThis();
	}
}
