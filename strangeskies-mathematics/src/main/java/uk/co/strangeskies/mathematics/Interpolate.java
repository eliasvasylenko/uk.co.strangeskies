package uk.co.strangeskies.mathematics;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.TriFunctionExpression;
import uk.co.strangeskies.mathematics.values.Value;

public class Interpolate<T, I> extends
		TriFunctionExpression<T, T, Value<?>, I> {
	public Interpolate(Expression<? extends T> from, Expression<? extends T> to,
			Expression<? extends Value<?>> delta,
			InterpolationFunction<? super T, ? extends I> interpolation) {
		super(from, to, delta, interpolation);
	}
}
