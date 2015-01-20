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
package uk.co.strangeskies.mathematics.expression.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.Observer;

public class ExpressionArrayList<E extends Expression<?>> extends ArrayList<E>
		implements ExpressionList<ExpressionArrayList<E>, E>,
		CopyDecouplingExpression<ExpressionArrayList<E>> {
	private static final long serialVersionUID = 1L;

	private boolean evaluated = true;

	private final Observer<Expression<?>> dependencyObserver;

	private final Set<Observer<? super Expression<ExpressionArrayList<E>>>> observers;

	private ReadWriteLock lock;

	public ExpressionArrayList() {
		dependencyObserver = message -> update();

		observers = new TreeSet<>();

		lock = new ReentrantReadWriteLock();
	}

	public ExpressionArrayList(Collection<E> expressions) {
		this();

		addAll(expressions);
	}

	protected final void update() {
		getLock().writeLock().lock();

		if (evaluated) {
			evaluated = false;
			postUpdate();
		}

		getLock().writeLock().unlock();
	}

	protected final void postUpdate() {
		for (Observer<?> observer : observers)
			observer.notify(null);
	}

	@Override
	public final boolean add(E expression) {
		getLock().writeLock().lock();

		boolean added = super.add(expression);
		if (added) {
			expression.addObserver(dependencyObserver);

			update();
		}

		getLock().writeLock().unlock();

		return added;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		getLock().writeLock().lock();

		boolean removed = super.remove(expression);
		if (removed) {
			((E) expression).removeObserver(dependencyObserver);

			update();
		}

		getLock().writeLock().unlock();

		return removed;
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		getLock().writeLock().lock();

		boolean changed = false;
		for (E expression : expressions)
			if (super.add(expression)) {
				expression.addObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getLock().writeLock().unlock();

		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		getLock().writeLock().lock();

		boolean changed = false;
		for (Object expression : expressions)
			if (super.remove(expression)) {
				((E) expression).removeObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getLock().writeLock().unlock();

		return changed;
	}

	@Override
	public final void clear() {
		clear(true);
	}

	protected final void clear(boolean update) {
		if (!isEmpty()) {
			getLock().writeLock().lock();

			for (E expression : this)
				expression.removeObserver(dependencyObserver);

			super.clear();

			if (update)
				update();

			getLock().writeLock().unlock();
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		getLock().writeLock().lock();

		clear(false);

		for (E expression : expressions)
			if (super.add(expression))
				expression.addObserver(dependencyObserver);

		update();

		getLock().writeLock().unlock();
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		getLock().writeLock().lock();

		TreeSet<E> toRemove = new TreeSet<>();

		toRemove.addAll(this);
		toRemove.removeAll(c);

		boolean changed = removeAll(toRemove);

		getLock().writeLock().unlock();

		return changed;
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableList(this);
	}

	@Override
	public final ExpressionArrayList<E> getValue() {
		getLock().readLock().lock();
		evaluated = true;
		getLock().readLock().unlock();

		return this;
	}

	@Override
	public final boolean addObserver(
			Observer<? super Expression<ExpressionArrayList<E>>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(
			Observer<? super Expression<ExpressionArrayList<E>>> observer) {
		return observers.remove(observer);
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	@Override
	public final ExpressionArrayList<E> copy() {
		getLock().writeLock().lock();
		ExpressionArrayList<E> copy = new ExpressionArrayList<>(this);
		getLock().writeLock().unlock();

		return copy;
	}

	@Override
	public final void add(int index, E expression) {
		getLock().writeLock().lock();

		super.add(index, expression);

		expression.addObserver(dependencyObserver);

		update();

		getLock().writeLock().unlock();
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> expressions) {
		getLock().writeLock().lock();

		for (E expression : expressions) {
			add(index++, expression);
			expression.addObserver(dependencyObserver);
		}

		getLock().writeLock().unlock();

		return !expressions.isEmpty();
	}

	@Override
	public final E remove(int index) {
		getLock().writeLock().lock();

		E removed = super.remove(index);

		removed.removeObserver(dependencyObserver);

		getLock().writeLock().unlock();

		return removed;
	}

	@Override
	public final E set(int index, E expression) {
		getLock().writeLock().lock();

		E removed = super.remove(index);
		super.add(index, expression);

		removed.removeObserver(dependencyObserver);
		expression.addObserver(dependencyObserver);

		getLock().writeLock().unlock();

		return removed;
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
}
