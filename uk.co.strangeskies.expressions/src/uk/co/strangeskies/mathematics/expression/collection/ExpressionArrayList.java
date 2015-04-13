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
import java.util.concurrent.locks.Lock;
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
		getWriteLock().lock();

		if (evaluated) {
			evaluated = false;
			postUpdate();
		}

		getWriteLock().unlock();
	}

	protected final void postUpdate() {
		for (Observer<?> observer : observers)
			observer.notify(null);
	}

	@Override
	public final boolean add(E expression) {
		getWriteLock().lock();

		boolean added = super.add(expression);
		if (added) {
			expression.addObserver(dependencyObserver);

			update();
		}

		getWriteLock().unlock();

		return added;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		getWriteLock().lock();

		boolean removed = super.remove(expression);
		if (removed) {
			((E) expression).removeObserver(dependencyObserver);

			update();
		}

		getWriteLock().unlock();

		return removed;
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		getWriteLock().lock();

		boolean changed = false;
		for (E expression : expressions)
			if (super.add(expression)) {
				expression.addObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getWriteLock().unlock();

		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		getWriteLock().lock();

		boolean changed = false;
		for (Object expression : expressions)
			if (super.remove(expression)) {
				((E) expression).removeObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getWriteLock().unlock();

		return changed;
	}

	@Override
	public final void clear() {
		clear(true);
	}

	protected final void clear(boolean update) {
		if (!isEmpty()) {
			getWriteLock().lock();

			for (E expression : this)
				expression.removeObserver(dependencyObserver);

			super.clear();

			if (update)
				update();

			getWriteLock().unlock();
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		getWriteLock().lock();

		clear(false);

		for (E expression : expressions)
			if (super.add(expression))
				expression.addObserver(dependencyObserver);

		update();

		getWriteLock().unlock();
	}

	@Override
	public final boolean retainAll(Collection<?> c) {
		getWriteLock().lock();

		TreeSet<E> toRemove = new TreeSet<>();

		toRemove.addAll(this);
		toRemove.removeAll(c);

		boolean changed = removeAll(toRemove);

		getWriteLock().unlock();

		return changed;
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableList(this);
	}

	@Override
	public final ExpressionArrayList<E> getValue() {
		getReadLock().lock();
		evaluated = true;
		getReadLock().unlock();

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
		getWriteLock().lock();
		ExpressionArrayList<E> copy = new ExpressionArrayList<>(this);
		getWriteLock().unlock();

		return copy;
	}

	@Override
	public final void add(int index, E expression) {
		getWriteLock().lock();

		super.add(index, expression);

		expression.addObserver(dependencyObserver);

		update();

		getWriteLock().unlock();
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> expressions) {
		getWriteLock().lock();

		for (E expression : expressions) {
			add(index++, expression);
			expression.addObserver(dependencyObserver);
		}

		getWriteLock().unlock();

		return !expressions.isEmpty();
	}

	@Override
	public final E remove(int index) {
		getWriteLock().lock();

		E removed = super.remove(index);

		removed.removeObserver(dependencyObserver);

		getWriteLock().unlock();

		return removed;
	}

	@Override
	public final E set(int index, E expression) {
		getWriteLock().lock();

		E removed = super.remove(index);
		super.add(index, expression);

		removed.removeObserver(dependencyObserver);
		expression.addObserver(dependencyObserver);

		getWriteLock().unlock();

		return removed;
	}

	@Override
	public Lock getReadLock() {
		return lock.readLock();
	}

	public Lock getWriteLock() {
		return lock.writeLock();
	}
}
