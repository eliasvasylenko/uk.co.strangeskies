package uk.co.strangeskies.mathematics.geometry.matrix;

import uk.co.strangeskies.mathematics.values.Value;

public interface MatrixS<S extends MatrixS<S, V>, V extends Value<V>> extends
		Matrix<S, V> {
	public V getDeterminant();

	public S transpose();

	public int getDimensions();
}
