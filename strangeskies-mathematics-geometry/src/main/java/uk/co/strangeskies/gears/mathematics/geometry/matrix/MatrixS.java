package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixS<S extends MatrixS<S, V>, V extends Value<V>> extends
		Matrix<S, V> {
	public V getDeterminant();

	public S transpose();

	public int getDimensions();
}
