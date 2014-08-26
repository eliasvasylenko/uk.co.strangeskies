package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixH3<V extends Value<V>> extends MatrixH<MatrixH3<V>, V> {
	@Override
	public Matrix3<V> getTransformationMatrix();

	@Override
	public List<Vector4<V>> getRowVectors();

	@Override
	public List<VectorH3<V>> getColumnVectors();

	@Override
	public Vector4<V> getRowVector(int row);

	@Override
	public VectorH3<V> getColumnVector(int column);
}