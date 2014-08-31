package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

public interface Rotatable2<S extends Rotatable2<S>> extends Self<S> {
	public S rotate(Value<?> angle);

	public S rotate(Value<?> angle, Vector2<?> centre);

	public default S getRotated(Value<?> angle) {
		return copy().rotate(angle);
	}

	public default S getRotated(Value<?> angle, Vector2<?> centre) {
		return copy().rotate(angle, centre);
	}
}
