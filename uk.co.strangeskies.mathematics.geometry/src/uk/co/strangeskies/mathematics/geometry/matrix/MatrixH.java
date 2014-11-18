package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.NonCommutativelyTranslatable;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.values.Value;

public interface MatrixH<S extends MatrixH<S, V>, V extends Value<V>> extends
		NonCommutativelyTranslatable<S>, MatrixS<S, V> {
	public int getProjectedDimensions();

	public MatrixNN<V> getMutableMatrix();

	public MatrixS<?, V> getTransformationMatrix();

	@Override
	public List<? extends VectorH<?, V>> getColumnVectors();

	@Override
	public VectorH<?, V> getColumnVector(int column);
}