package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.geometry.Rotatable2;
import uk.co.strangeskies.mathematics.values.Value;

public interface VectorH2<V extends Value<V>> extends VectorH<VectorH2<V>, V>,
		Rotatable2<VectorH2<V>> {
	public V getX();

	public V getY();

	public V getW();
}