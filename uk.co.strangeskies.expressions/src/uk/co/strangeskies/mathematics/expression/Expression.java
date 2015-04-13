/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.concurrent.locks.Lock;

import uk.co.strangeskies.utilities.Observable;
import uk.co.strangeskies.utilities.Observer;

/**
 * <p>
 * An expression for use in reactive programming.
 * </p>
 * 
 * <p>
 * This class is intended to be {@link Observable} over a specific behaviour:
 * its {@link Observer}s should be notified any time the expression changes.
 * More precisely, they should be notified at any moment at which the value
 * which would be returned from a call getValue() is different from the value
 * which would have been returned before.
 * </p>
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the result value of this expression.
 */
public interface Expression<T> extends Observable<Expression<T>> {
	/**
	 * <p>
	 * This should always return the correct value for this Expression. Be careful
	 * to remember that the object returned from getValue() should not be an
	 * updatable reference, i.e. it should be either an immutable class or a const
	 * reference. This is important, but it does not mean that the return value
	 * can necessarily be relied upon not to change when this expression is
	 * updated.
	 * </p>
	 *
	 * <p>
	 * Once a value has been returned, it is up to the implementing Expression as
	 * to whether the value will be reliable such that it will remain the same
	 * even if the conceptual value of this expression subsequently changes, or
	 * whether it will update automatically with the expression. Please only rely
	 * on either behaviour if it is explicitly documented, otherwise copy the
	 * returned value if you need a persistent reference which is safe to update
	 * and safe from updates, or re-fetch the result when you need the updated
	 * value.
	 * </p>
	 *
	 * <p>
	 * Neither behaviour is explicitly required in preference to the other as much
	 * of the time it could be very wasteful to require a value be copied, but at
	 * the same time, it could often be impossible to return a persistently linked
	 * value, e.g. for the case of a value type which is an immutable class, such
	 * as String. It is expected, though, that the result remain valid at least
	 * until the observer callback returns, though a lock should be obtained at
	 * this point to guarantee any held references will remain valid without a
	 * copy.
	 * </p>
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
	 * @param value
	 * @return An immutable expression whose value is always that given, and upon
	 *         which read locks are always available.
	 */
	public static <T> Expression<T> immutable(final T value) {
		return new Expression<T>() {
			@Override
			public final void clearObservers() {}

			@Override
			public final T getValue() {
				return value;
			}

			@Override
			public final boolean addObserver(Observer<? super Expression<T>> observer) {
				return true;
			}

			@Override
			public final boolean removeObserver(
					Observer<? super Expression<T>> observer) {
				return true;
			}

			@Override
			public Lock getReadLock() {
				return new ImmutableReadWriteLock();
			}
		};
	}

	/**
	 * @return A read lock on the current value of this {@link Expression}.
	 *         Implementing classes are responsible for providing access to write
	 *         locks if and where appropriate.
	 */
	public Lock getReadLock();
}
