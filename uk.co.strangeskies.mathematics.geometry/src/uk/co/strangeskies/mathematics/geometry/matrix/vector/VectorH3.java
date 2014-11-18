package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.values.Value;

public interface VectorH3<V extends Value<V>> extends VectorH<VectorH3<V>, V> {
	public default V getX() {
		return getElement(0);
	}

	public default V getY() {
		return getElement(1);
	}

	public default V getW() {
		return getElement(2);
	}

	@Override
	public Vector3<V> getMutableVector();
}