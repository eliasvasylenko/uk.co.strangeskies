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
package uk.co.strangeskies.mathematics.expression.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Consumer;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.collection.AbstractObservableArrayList;

public class ExpressionArrayList<E extends Expression<?, ?>>
		extends AbstractObservableArrayList<ExpressionArrayList<E>, E> implements ExpressionList<ExpressionArrayList<E>, E>,
		CopyDecouplingExpression<ExpressionArrayList<E>, ExpressionArrayList<E>> {
	private static final long serialVersionUID = 1L;

	private int evaluated = 0;

	private final Consumer<Expression<?, ?>> dependencyObserver;

	private ReentrantReadWriteLock lock;

	public ExpressionArrayList() {
		dependencyObserver = message -> fireEvent();

		lock = new ReentrantReadWriteLock();
	}

	public ExpressionArrayList(Collection<E> expressions) {
		this();

		addAll(expressions);
	}

	@Override
	protected boolean beginChange() {
		boolean started;

		if (!lock.writeLock().isHeldByCurrentThread()) {
			getWriteLock();

			started = super.beginChange();

			if (!started) {
				throw new AssertionError();
			}
		} else {
			started = super.beginChange();

			if (started) {
				throw new AssertionError();
			}
		}

		return started;
	}

	@Override
	protected boolean endChange() {
		if (!lock.writeLock().isHeldByCurrentThread()) {
			throw new IllegalStateException();
		}

		boolean ended = super.endChange();

		if (ended) {
			unlockWriteLock();
		}

		return ended;
	}

	@Override
	protected boolean fireChange(Change<E> change) {
		if (evaluated < 0) {
			return false;
		}

		// TODO
		int wasEvaluatedAt = evaluated;
		return super.fireChange(new Change<E>() {
			void validate() {
				if (wasEvaluatedAt == evaluated) {
					
				}
			}

			@Override
			public boolean next() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public int index() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public List<E> getRemoved() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<E> getAdded() {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	protected final void postUpdate() {}

	@Override
	public final boolean add(E expression) {
		try {
			getWriteLock().lock();

			super.add(expression);
			expression.addObserver(dependencyObserver);

			return true;
		} finally {
			unlockWriteLock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		try {
			getWriteLock().lock();

			boolean removed = super.remove(expression);
			if (removed) {
				((E) expression).removeObserver(dependencyObserver);
			}

			return removed;
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		try {
			getWriteLock().lock();

			boolean changed = false;
			for (E expression : expressions)
				if (super.add(expression)) {
					expression.addObserver(dependencyObserver);
					changed = true;
				}

			return changed;
		} finally {
			unlockWriteLock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		try {
			getWriteLock().lock();

			boolean changed = false;
			for (Object expression : expressions)
				if (super.remove(expression)) {
					((E) expression).removeObserver(dependencyObserver);
					changed = true;
				}

			return changed;
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final void clear() {
		try {
			getWriteLock().lock();
			if (!isEmpty()) {

				for (E expression : this)
					expression.removeObserver(dependencyObserver);

				super.clear();
			}
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		for (E expression : this)
			expression.removeObserver(dependencyObserver);

		super.clear();

		addAll(expressions);
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		try {
			getWriteLock().lock();

			for (E expression : this)
				if (!c.contains(expression))
					expression.removeObserver(dependencyObserver);

			return super.retainAll(c);
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableList(this);
	}

	@Override
	public final ExpressionArrayList<E> getValue() {
		try {
			getReadLock().lock();
			if (evaluated < 0) {
				evaluated = 1 - evaluated;
			}

			return this;
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public final ExpressionArrayList<E> copy() {
		try {
			getWriteLock().lock();
			return new ExpressionArrayList<>(this);
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final void add(int index, E expression) {
		try {
			getWriteLock().lock();

			super.add(index, expression);

			expression.addObserver(dependencyObserver);
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> expressions) {
		try {
			getWriteLock().lock();

			for (E expression : expressions) {
				expression.addObserver(dependencyObserver);
			}
			super.addAll(index, expressions);
		} finally {
			unlockWriteLock();
		}

		return !expressions.isEmpty();
	}

	@Override
	public final E remove(int index) {
		try {
			getWriteLock().lock();

			E removed = super.remove(index);

			removed.removeObserver(dependencyObserver);

			return removed;
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final E set(int index, E expression) {
		try {
			getWriteLock().lock();

			E previous = super.set(index, expression);

			previous.removeObserver(dependencyObserver);
			expression.addObserver(dependencyObserver);

			return previous;
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public ReadLock getReadLock() {
		return lock.readLock();
	}

	protected WriteLock getWriteLock() {
		return lock.writeLock();
	}

	protected void unlockWriteLock() {
		while (getWriteLock().isHeldByCurrentThread())
			getWriteLock().unlock();
	}
}
