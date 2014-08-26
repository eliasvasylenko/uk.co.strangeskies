package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.impl.MatrixNImpl;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface MatrixHN<V extends Value<V>> extends MatrixH<MatrixHN<V>, V> {
	@Override
	public MatrixNImpl<V> getTransformationMatrix();

	@Override
	public Vector<?, V> getMajorVector(int index);

	@Override
	public Vector<?, V> getMinorVector(int index);
}