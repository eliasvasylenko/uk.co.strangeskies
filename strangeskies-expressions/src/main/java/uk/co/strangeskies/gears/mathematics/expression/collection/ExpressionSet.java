package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.Set;

import uk.co.strangeskies.gears.mathematics.expression.Expression;

public interface ExpressionSet<S extends ExpressionSet<S, E>, E extends Expression<?>>
		extends Set<E>, ExpressionCollection<S, E> {
}
