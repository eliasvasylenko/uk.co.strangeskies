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
