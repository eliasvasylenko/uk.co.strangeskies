package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixNN<V extends Value<V>> extends Matrix<MatrixNN<V>, V> {
	@Override
	public MatrixNN<V> getTransposed();

	@Override
	public List<VectorNImpl<V>> getRowVectors();

	@Override
	public List<VectorNImpl<V>> getColumnVectors();

	@Override
	public VectorNImpl<V> getRowVector(int row);

	@Override
	public VectorNImpl<V> getColumnVector(int column);

	@Override
	public List<VectorNImpl<V>> getMajorVectors();

	@Override
	public List<VectorNImpl<V>> getMinorVectors();

	@Override
	public VectorNImpl<V> getMajorVector(int index);

	@Override
	public VectorNImpl<V> getMinorVector(int index);
}