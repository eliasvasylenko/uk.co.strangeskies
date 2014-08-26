package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Matrix4<V extends Value<V>> extends MatrixS<Matrix4<V>, V> {
	@Override
	public List<Vector4<V>> getRowVectors();

	@Override
	public List<Vector4<V>> getColumnVectors();

	@Override
	public Vector4<V> getRowVector(int row);

	@Override
	public Vector4<V> getColumnVector(int column);

	@Override
	public List<Vector4<V>> getMajorVectors();

	@Override
	public List<Vector4<V>> getMinorVectors();

	@Override
	public Vector4<V> getMajorVector(int index);

	@Override
	public Vector4<V> getMinorVector(int index);
}