package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixH2;
import uk.co.strangeskies.gears.mathematics.values.Value;

public interface AffineTransformation2<S extends AffineTransformation2<S, V>, V extends Value<V>>
		extends Expression<MatrixH2<V>> {
}
