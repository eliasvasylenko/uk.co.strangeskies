package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.mathematics.values.Value;

public interface MatrixNN<V extends Value<V>> extends Matrix<MatrixNN<V>, V> {
	@Override
	public MatrixNN<V> getTransposed();

	@Override
	public List<VectorN<V>> getRowVectors();

	@Override
	public List<VectorN<V>> getColumnVectors();

	@Override
	public VectorN<V> getRowVector(int row);

	@Override
	public VectorN<V> getColumnVector(int column);

	@Override
	public List<VectorN<V>> getMajorVectors();

	@Override
	public List<VectorN<V>> getMinorVectors();

	@Override
	public VectorN<V> getMajorVector(int index);

	@Override
	public VectorN<V> getMinorVector(int index);
}