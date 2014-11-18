package uk.co.strangeskies.mathematics.expression.collection;

import java.util.List;

import uk.co.strangeskies.mathematics.expression.Expression;

public interface ExpressionList<S extends ExpressionList<S, E>, E extends Expression<?>>
		extends List<E>, ExpressionCollection<S, E> {
}
