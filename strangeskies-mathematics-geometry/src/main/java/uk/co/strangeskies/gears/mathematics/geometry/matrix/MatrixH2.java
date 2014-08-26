package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.NonCommutativelyRotatable2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixH2<V extends Value<V>> extends MatrixH<MatrixH2<V>, V>,
		NonCommutativelyRotatable2<MatrixH2<V>> {
	@Override
	public Matrix2<V> getTransformationMatrix();

	@Override
	public List<Vector3<V>> getRowVectors();

	@Override
	public List<VectorH2<V>> getColumnVectors();

	@Override
	public Vector3<V> getRowVector(int row);

	@Override
	public VectorH2<V> getColumnVector(int column);
}