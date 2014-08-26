package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Matrix3<V extends Value<V>> extends MatrixS<Matrix3<V>, V> {
	@Override
	public List<Vector3<V>> getRowVectors();

	@Override
	public List<Vector3<V>> getColumnVectors();

	@Override
	public Vector3<V> getRowVector(int row);

	@Override
	public Vector3<V> getColumnVector(int column);

	@Override
	public List<Vector3<V>> getMajorVectors();

	@Override
	public List<Vector3<V>> getMinorVectors();

	@Override
	public Vector3<V> getMajorVector(int index);

	@Override
	public Vector3<V> getMinorVector(int index);
}