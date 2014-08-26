package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.NonCommutativelyRotatable2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface Matrix2<V extends Value<V>> extends MatrixS<Matrix2<V>, V>,
		NonCommutativelyRotatable2<Matrix2<V>> {
	@Override
	public List<Vector2<V>> getRowVectors();

	@Override
	public List<Vector2<V>> getColumnVectors();

	@Override
	public Vector2<V> getRowVector(int row);

	@Override
	public Vector2<V> getColumnVector(int column);

	@Override
	public List<Vector2<V>> getMajorVectors();

	@Override
	public List<Vector2<V>> getMinorVectors();

	@Override
	public Vector2<V> getMajorVector(int index);

	@Override
	public Vector2<V> getMinorVector(int index);
}