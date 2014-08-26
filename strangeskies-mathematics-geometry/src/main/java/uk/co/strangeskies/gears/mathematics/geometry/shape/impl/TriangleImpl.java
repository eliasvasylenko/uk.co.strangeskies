package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Triangle;
import uk.co.strangeskies.gears.mathematics.values.Value;

public class TriangleImpl<V extends Value<V>> extends
		SimplePolygonImpl<Triangle<V>, V> implements Triangle<V> {
	private Vector2<V> a;
	private Vector2<V> b;
	private Vector2<V> c;

	public TriangleImpl(Vector2<V> a, Vector2<V> b, Vector2<V> c) {
		this.a = a.copy();
		this.b = b.copy();
		this.c = c.copy();
	}

	public TriangleImpl(Triangle<V> other) {
		this(other.getA(), other.getB(), other.getC());
	}

	@Override
	public Triangle<V> set(Vector2<V> a, Vector2<V> b, Vector2<V> c) {
		this.a = a;
		this.b = b;
		this.c = c;

		return this;
	}

	@Override
	public Triangle<V> set(Number ax, Number ay, Number bx, Number by, Number cx,
			Number cy) {
		a.setData(ax, ay);
		b.setData(bx, by);
		c.setData(cx, cy);

		return this;
	}

	@Override
	public Triangle<V> set(Triangle<V> to) {
		return set(to.getA(), to.getB(), to.getC());
	}

	@Override
	public void setA(Vector2<V> a) {
		this.a = a;
	}

	@Override
	public void setB(Vector2<V> b) {
		this.b.set(b);
	}

	@Override
	public void setC(Vector2<V> c) {
		this.c.set(c);
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
	public Vector2<V> getC() {
		return c;
	}

	@Override
	public List<Vector2<V>> getVertices() {
		List<Vector2<V>> vertices = new ArrayList<>();
		vertices.add(a);
		vertices.add(b);
		vertices.add(c);
		return vertices;
	}

	@Override
	public Triangle<V> copy() {
		return new TriangleImpl<>(this);
	}
}
