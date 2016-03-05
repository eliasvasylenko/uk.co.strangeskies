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
import java.util.LinkedHashSet;
import java.util.Set;
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

	private boolean evaluated = true;

	private final Consumer<Expression<?, ?>> dependencyObserver;

	private final Set<Consumer<? super ExpressionArrayList<E>>> observers;

	private ReentrantReadWriteLock lock;

	public ExpressionArrayList() {
		dependencyObserver = message -> update();

		observers = new LinkedHashSet<>();

		lock = new ReentrantReadWriteLock();
	}

	public ExpressionArrayList(Collection<E> expressions) {
		this();

		addAll(expressions);
	}

	protected final void update() {
		try {
			getWriteLock().lock();

			if (evaluated) {
				evaluated = false;
				postUpdate();
			}

		} finally {
			unlockWriteLock();
		}
	}

	protected final void postUpdate() {
		for (Consumer<?> observer : observers)
			observer.accept(null);
	}

	@Override
	public final boolean add(E expression) {
		try {
			getWriteLock().lock();

			super.add(expression);
			expression.addObserver(dependencyObserver);

			update();

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

				update();
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

			if (changed)
				update();

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

			if (changed)
				update();

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

				update();
			}
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		try {
			getWriteLock().lock();

			for (E expression : this)
				expression.removeObserver(dependencyObserver);

			super.clear();

			if (!addAll(expressions)) {
				update();
			}
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		try {
			getWriteLock().lock();

			for (E expression : this)
				if (!c.contains(expression))
					expression.removeObserver(dependencyObserver);

			boolean changed = super.retainAll(c);

			if (changed) {
				update();
			}

			return changed;
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
			evaluated = true;

			return this;
		} finally {
			getReadLock().unlock();
		}
	}

	@Override
	public final boolean addObserver(Consumer<? super ExpressionArrayList<E>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(Consumer<? super ExpressionArrayList<E>> observer) {
		return observers.remove(observer);
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

			update();
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
			if (super.addAll(index, expressions)) {
				update();
			}
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

			update();

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

			update();

			return previous;
		} finally {
			unlockWriteLock();
		}
	}

	@Override
	public ReadLock getReadLock() {
		return lock.readLock();
	}

	public WriteLock getWriteLock() {
		return lock.writeLock();
	}

	protected void unlockWriteLock() {
		while (getWriteLock().isHeldByCurrentThread())
			getWriteLock().unlock();
	}
}
