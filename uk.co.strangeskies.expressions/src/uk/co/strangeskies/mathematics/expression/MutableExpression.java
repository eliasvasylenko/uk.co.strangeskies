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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.Observer;

public abstract class MutableExpression<T> implements Expression<T> {
	private final Set<Observer<? super Expression<T>>> observers;
	private final ReadWriteLock lock;

	public MutableExpression() {
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
		getLock().writeLock().lock();
		for (Observer<? super Expression<T>> observer : observers)
			observer.notify(null);
		getLock().writeLock().unlock();
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
}
