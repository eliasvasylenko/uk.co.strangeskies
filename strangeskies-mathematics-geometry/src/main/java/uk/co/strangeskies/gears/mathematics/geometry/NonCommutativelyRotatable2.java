package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface NonCommutativelyRotatable2<S extends NonCommutativelyRotatable2<S>>
		extends Rotatable2<S> {
	public S preRotate(Value<?> value);

	public S preRotate(Value<?> angle, Vector2<?> centre);

	public S getPreRotated(Value<?> value);

	public S getPreRotated(Value<?> angle, Vector2<?> centre);
}
