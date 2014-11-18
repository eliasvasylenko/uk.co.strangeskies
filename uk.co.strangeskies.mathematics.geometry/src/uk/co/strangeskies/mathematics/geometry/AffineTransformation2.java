package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH2;
import uk.co.strangeskies.mathematics.values.Value;

public interface AffineTransformation2<S extends AffineTransformation2<S, V>, V extends Value<V>>
		extends Expression<MatrixH2<V>> {
}
