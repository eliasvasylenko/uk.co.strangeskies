/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public interface Triangle<V extends Value<V>> extends
/*  */ConvexPolygon<Triangle<V>, V> {
	public Triangle<V> set(Vector2<V> a, Vector2<V> b, Vector2<V> c);

	public Triangle<V> set(Number ax, Number ay, Number bx, Number by, Number cx,
			Number cy);

	public void setA(/*  */Vector2<V> a);

	public void setB(/*  */Vector2<V> b);

	public void setC(/*  */Vector2<V> c);

	public Vector2<V> getA() /*  */;

	public Vector2<V> getB() /*  */;

	public Vector2<V> getC() /*  */;
}