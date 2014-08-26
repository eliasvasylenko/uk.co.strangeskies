package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.values.Value;

public interface VectorH3<V extends Value<V>> extends VectorH<VectorH3<V>, V> {
	public V getX();

	public V getY();

	public V getW();
}