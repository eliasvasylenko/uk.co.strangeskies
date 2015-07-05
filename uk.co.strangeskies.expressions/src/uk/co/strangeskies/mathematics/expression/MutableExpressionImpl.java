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

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.Observer;

/**
 * An abstract class to help designing mutable expression, implementing a simple
 * observer list, locking mechanism, and update mechanism.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public abstract class MutableExpressionImpl<T> implements MutableExpression<T> {
	private final Set<Observer<? super Expression<T>>> observers;
	private final ReentrantReadWriteLock lock;

	private boolean dirty;

	/**
	 * Default constructor.
	 */
	public MutableExpressionImpl() {
		observers = new TreeSet<Observer<? super Expression<T>>>(
				new IdentityComparator<>());
		lock = new ReentrantReadWriteLock();
	}

	@Override
	public final boolean addObserver(Observer<? super Expression<T>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(Observer<? super Expression<T>> observer) {
		return observers.remove(observer);
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	protected final void postUpdate() {
		getWriteLock().lock();

		try {
			if (!dirty) {
				dirty = true;
				for (Observer<? super Expression<T>> observer : observers)
					observer.notify(null);
			}
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final T getValue() {
		try {
			getReadLock().lock();

			boolean dirty = this.dirty;
			if (this.dirty)
				this.dirty = false;
			T value = getValueImpl(dirty);

			return value;
		} finally {
			getReadLock().unlock();
		}
	}

	protected boolean isDirty() {
		return dirty;
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
	protected abstract T getValueImpl(boolean dirty);

	@Override
	public ReadLock getReadLock() {
		return lock.readLock();
	}

	@Override
	public WriteLock getWriteLock() {
		return lock.writeLock();
	}

	protected void unlockWriteLock() {
		while (getWriteLock().isHeldByCurrentThread())
			getWriteLock().unlock();
	}
}
