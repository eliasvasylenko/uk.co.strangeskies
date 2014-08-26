package uk.co.strangeskies.gears.mathematics.expression.collection;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.expression.Expression;

public interface ExpressionList<S extends ExpressionList<S, E>, E extends Expression<?>>
		extends List<E>, ExpressionCollection<S, E> {
}
