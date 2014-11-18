package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public interface NonCommutativelyRotatable2<S extends NonCommutativelyRotatable2<S>>
		extends Rotatable2<S> {
	public S preRotate(Value<?> value);

	public S preRotate(Value<?> angle, Vector2<?> centre);

	public default S getPreRotated(Value<?> value) {
		return copy().getPreRotated(value);
	}

	public default S getPreRotated(Value<?> angle, Vector2<?> centre) {
		return copy().getPreRotated(angle, centre);
	}
}
