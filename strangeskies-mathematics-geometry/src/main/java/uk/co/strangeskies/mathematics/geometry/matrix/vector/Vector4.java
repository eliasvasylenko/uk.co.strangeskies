package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.values.Value;

public interface Vector4<V extends Value<V>> extends Vector<Vector4<V>, V> {
	public V getX();

	public V getY();

	public V getZ();

	public V getW();
}