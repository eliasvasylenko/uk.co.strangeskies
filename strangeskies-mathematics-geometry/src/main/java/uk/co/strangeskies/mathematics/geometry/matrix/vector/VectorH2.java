package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.geometry.Rotatable2;
import uk.co.strangeskies.mathematics.values.Value;

public interface VectorH2<V extends Value<V>> extends VectorH<VectorH2<V>, V>,
		Rotatable2<VectorH2<V>> {
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
	public Vector2<V> getMutableVector();
}