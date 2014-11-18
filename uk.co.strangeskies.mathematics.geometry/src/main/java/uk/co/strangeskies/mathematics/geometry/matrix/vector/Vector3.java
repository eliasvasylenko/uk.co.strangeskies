package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.values.Value;

public interface Vector3<V extends Value<V>> extends Vector<Vector3<V>, V> {
	public default V getX() {
		return getElement(0);
	}

	public default V getY() {
		return getElement(1);
	}

	public default V getZ() {
		return getElement(2);
	}
}