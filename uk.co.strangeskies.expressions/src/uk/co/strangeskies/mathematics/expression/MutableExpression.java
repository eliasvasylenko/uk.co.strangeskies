package uk.co.strangeskies.mathematics.expression;

import java.util.concurrent.locks.Lock;

import uk.co.strangeskies.utilities.Observer;

/**
 * <p>
 * A basic interface for mutable {@link Expression} implementations.
 * Implementing classes are responsible for making sure write locks are held for
 * mutating operations, and for notifications to {@link Observer}s.
 * </p>
 * 
 * <p>
 * A mutating operation is considered to be any method or section of code which
 * can be considered to atomically result in a change in the value of this
 * {@link Expression}.
 * </p>
 * 
 * @author Elias N Vasylenko
 * @param <T>
 */
public interface MutableExpression<T> extends Expression<T> {
	/**
	 * @return A write lock which must be obtained before attempting to mutate
	 *         this {@link Expression}.
	 */
	public Lock getWriteLock();
}
