package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.Self;

public interface Rotatable2<S extends Rotatable2<S>> extends Self<S> {
	public S rotate(Value<?> angle);

	public S rotate(Value<?> angle, Vector2<?> centre);

	public S getRotated(Value<?> angle);

	public S getRotated(Value<?> angle, Vector2<?> centre);
}
