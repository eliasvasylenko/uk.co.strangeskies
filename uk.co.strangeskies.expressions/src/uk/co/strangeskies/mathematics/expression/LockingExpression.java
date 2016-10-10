/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An abstract class to help designing mutable expression, implementing a simple
 * observer list, locking mechanism, and update mechanism.
 * 
 * <p>
 * After mutation the held write lock should be downgraded to a read lock, then
 * observers should be notified, then the read lock should be released.
 * 
 * <p>
 * A mutating operation is considered to be any method or section of code which
 * can be considered to atomically result in a change in the value of this
 * {@link Expression}.
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound of the expression, i.e. the type of the expression
 *          object itself
 * @param <T>
 *          The type of the value of this expression
 */
public abstract class LockingExpression<S extends Expression<S, T>, T> extends DependentExpression<S, T>
		implements Expression<S, T> {
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	protected <U> U read(Supplier<U> read) {
		getReadLock().lock();

		try {
			return read.get();
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	protected boolean beginWrite() {
		getWriteLock().lock();
		return super.beginWrite();
	}

	@Override
	protected boolean endWrite() {
		getReadLock().lock();
		getWriteLock().unlock();

		try {
			return super.endWrite();
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public final T getValue() {
		getReadLock().lock();

		try {
			return super.getValue();
		} finally {
			getReadLock().unlock();
		}
	}

	public ReadLock getReadLock() {
		return lock.readLock();
	}

	protected WriteLock getWriteLock() {
		return lock.writeLock();
	}

	@Override
	public boolean addObserver(Consumer<? super S> observer) {
		getReadLock().lock();

		try {
			return super.addObserver(observer);
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public boolean removeObserver(Consumer<? super S> observer) {
		getReadLock().lock();

		try {
			return super.removeObserver(observer);
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public void clearObservers() {
		getReadLock().lock();

		try {
			super.clearObservers();
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public void fire(S item) {
		getReadLock().lock();

		try {
			super.fire(item);
		} finally {
			getReadLock().unlock();
		}
	}
}
