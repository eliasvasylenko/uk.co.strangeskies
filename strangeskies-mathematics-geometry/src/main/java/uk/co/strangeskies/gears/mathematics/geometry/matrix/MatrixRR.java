package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixRR<V extends Value<V>> extends Matrix<MatrixRR<V>, V> {
	@Override
	public MatrixRR<V> getTransposed();

	public MatrixRR<V> resize(Vector<?, IntValue> dimensions);

	public MatrixRR<V> resize(int rows, int columns);

	public MatrixRR<V> resizeColumns(int dimensions);

	public MatrixRR<V> resizeMajor(int dimensions);

	public MatrixRR<V> resizeMinor(int dimensions);

	public MatrixRR<V> resizeRows(int dimensions);

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