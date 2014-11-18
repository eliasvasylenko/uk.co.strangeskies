package uk.co.strangeskies.mathematics.expression.collection;

import java.util.SortedSet;

import uk.co.strangeskies.mathematics.expression.Expression;

public interface SortedExpressionSet<S extends SortedExpressionSet<S, E>, E extends Expression<?>>
		extends ExpressionSet<S, E>, SortedSet<E> {
}
