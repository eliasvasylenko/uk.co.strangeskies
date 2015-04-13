package uk.co.strangeskies.mathematics.expression;

import java.util.concurrent.locks.Lock;

public interface MutableExpression<T> extends Expression<T> {
	public Lock getWriteLock();
}
