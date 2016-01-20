/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.shape.impl;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.geometry.shape.Triangle;
import uk.co.strangeskies.mathematics.values.Value;

public class TriangleImpl<V extends Value<V>> implements Triangle<V> {
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
	public ClosedPolyline2<V> boundary() {
		ClosedPolyline2<V> boundary = new ClosedPolyline2Impl<>();
		boundary.vertices().add(a);
		boundary.vertices().add(b);
		boundary.vertices().add(c);
		return boundary;
	}

	@Override
	public Triangle<V> copy() {
		return new TriangleImpl<>(this);
	}

	@Override
	public Set<Vector2<V>> hullVertexSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Vector2<V>> vertexSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public uk.co.strangeskies.mathematics.geometry.shape.SimplePolygon.WindingDirection getWindingDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Triangle<V> getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock getReadLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addObserver(
			Consumer<? super Expression<Triangle<V>>> observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeObserver(
			Consumer<? super Expression<Triangle<V>>> observer) {
		// TODO Auto-generated method stub
		return false;
	}
}
