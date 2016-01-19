/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.Observer;
import java.util.concurrent.locks.Lock;

import uk.co.strangeskies.utilities.Observable;

/**
 * <p>
 * An expression for use in reactive programming. An expression has a
 * <em>value</em> which can be <em>resolved</em> through the invocation of
 * {@link #getValue()} or {@link #decoupleValue()}.
 * 
 * <p>
 * This class is intended to be {@link Observable} over a specific behaviour:
 * its {@link Observer}s should be notified any time the value of the expression
 * changes. More precisely, they should be notified at the point that the value
 * which <em>would</em> be returned from a future invocation of
 * {@link #getValue()} or {@link #decoupleValue()} becomes different from the
 * previous value resolved through either of those methods.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the value of this expression.
 */
public interface Expression<T> extends Observable<Expression<T>> {
	/**
	 * This should always return the correct current value for this Expression. Be
	 * careful to remember that the reference returned should not be able to
	 * mutate this {@link Expression}, i.e. it should be either an immutable
	 * class, a const reference, or a copy of the underlying value. This is
	 * important, but conversely it does <em>not</em> mean that the return value
	 * can necessarily be relied upon not to not mutate when this expression is
	 * updated, unless a read lock is held.
	 *
	 * <p>
	 * Once a value has been returned, it is up to the implementing Expression as
	 * to whether the value will be reliable such that it will remain the same
	 * even if the conceptual value of this expression subsequently changes, or
	 * whether it will update automatically with the expression. Please only rely
	 * on either behaviour if it is explicitly documented, otherwise use
	 * {@link #decoupleValue()} if you need a persistent reference which is safe
	 * <em>to</em> mutate and/or safe <em>from</em> external mutation.
	 * 
	 * <p>
	 * The observers should only ever be notified of an update from the thread
	 * which has the write lock on an {@link Expression}, and {@link Expression}s
	 * should be careful to only notify observers when they are in a state where
	 * their value can be fetched. Immediate fetch is discouraged, though.
	 * Expressions should generally update lazily, not eagerly.
	 * 
	 * @return The fully evaluated value of this Expression at the time of method
	 *         invocation.
	 */
	public T getValue();

	/**
	 * @return A value which is equal to the result of {@link #getValue()} at time
	 *         of invocation, with the added guarantee that it will not be further
	 *         mutated by this {@link Expression}.
	 */
	public default T decoupleValue() {
		return getValue();
	}

	/**
	 * Create an immutable {@link Expression} instance whose value is always that
	 * given, and upon which read locks are always available. Further, an observer
	 * set is not maintained, as observers will never receive notifications, so
	 * attempting to add or remove them will always return {@code true}.
	 * 
	 * @param <T>
	 *          The type of the expression.
	 * @param value
	 *          The value of the new immutable {@link Expression}.
	 * @return An immutable {@link Expression} instance whose value is always that
	 *         given, and upon which read locks are always available.
	 */
	public static <T> Expression<T> immutable(final T value) {
		return new ImmutableExpressionImpl<T>() {
			@Override
			public final T getValue() {
				return value;
			}
		};
	}

	/**
	 * @return
	 * 				<p>
	 *         A read lock on the current value of this {@link Expression}.
	 *         Implementing classes are responsible for making sure attempts to
	 *         resolve the value of this expression obtain a read lock, and for
	 *         providing access to write locks if and where appropriate.
	 * 
	 *         <p>
	 *         {@link MutableExpression} provides a simple interface over write
	 *         locking behaviour.
	 */
	public Lock getReadLock();
}
