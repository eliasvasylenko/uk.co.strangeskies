package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.mathematics.values.Value;

public interface VectorH3<V extends Value<V>> extends VectorH<VectorH3<V>, V> {
	@Override
	public default Vector3<V> getMutableVector() {
		Vector3<V> mutableVector = new Vector3Impl<V>(getOrder(), getOrientation(),
				getData().subList(0, 3));

		return mutableVector;
	}

	public default V getX() {
		return getElement(0);
	}

	public default V getY() {
		return getElement(1);
	}

	public default V getW() {
		return getElement(2);
	}
}