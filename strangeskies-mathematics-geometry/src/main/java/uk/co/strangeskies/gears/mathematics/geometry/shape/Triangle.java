package uk.co.strangeskies.gears.mathematics.geometry.shape;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Triangle<V extends Value<V>> extends
		SimplePolygon<Triangle<V>, V> {
	public Triangle<V> set(Vector2<V> a, Vector2<V> b, Vector2<V> c);

	public Triangle<V> set(Number ax, Number ay, Number bx, Number by, Number cx,
			Number cy);

	public void setA(/*@ReadOnly*/Vector2<V> a);

	public void setB(/*@ReadOnly*/Vector2<V> b);

	public void setC(/*@ReadOnly*/Vector2<V> c);

	public Vector2<V> getA() /*@ReadOnly*/;

	public Vector2<V> getB() /*@ReadOnly*/;

	public Vector2<V> getC() /*@ReadOnly*/;
}
