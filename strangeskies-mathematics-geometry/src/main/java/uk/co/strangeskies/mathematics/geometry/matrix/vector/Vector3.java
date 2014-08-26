package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.values.Value;

public interface Vector3<V extends Value<V>> extends Vector<Vector3<V>, V> {
	public V getX();

	public V getY();

	public V getZ();
}