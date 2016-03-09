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

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * An abstract class to help designing mutable expression, implementing a simple
 * observer list, locking mechanism, and update mechanism.
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound of the expression, i.e. the type of the expression
 *          object itself
 * @param <T>
 *          The type of the value of this expression
 */
public abstract class LockingExpressionImpl<S extends Expression<S, T>, T> extends ExpressionImpl<S, T>
		implements LockingExpression<S, T> {
	private final ReentrantReadWriteLock lock;

	/**
	 * Default constructor.
	 */
	public LockingExpressionImpl() {
		lock = new ReentrantReadWriteLock();
	}

	@Override
	protected boolean beginChange() {
		getWriteLock().lock();
		return super.beginChange();
	}

	@Override
	protected boolean endChange() {
		boolean ended = super.endChange();
		getWriteLock().unlock();
		return ended;
	}

	@Override
	protected final boolean fireChange() {
		try {
			getReadLock().lock();
			getWriteLock().unlock();

			return super.fireChange();
		} finally {
			getWriteLock().unlock();
			getReadLock().unlock();
		}
	}

	@Override
	public final T getValue() {
		try {
			getReadLock().lock();

			return super.getValue();
		} finally {
			getReadLock().unlock();
		}
	}

	/**
	 * Implementing classes should compute the value of the {@link Expression}
	 * here. Read lock is guaranteed to be obtained. This method should never be
	 * invoked manually.
	 * 
	 * @param dirty
	 *          Whether the expression has been mutated since this method was last
	 *          invoked.
	 * @return The value of this {@link Expression}.
	 */
	@Override
	protected abstract T getValueImpl(boolean dirty);

	@Override
	public ReadLock getReadLock() {
		return lock.readLock();
	}

	@Override
	public WriteLock getWriteLock() {
		return lock.writeLock();
	}
}
