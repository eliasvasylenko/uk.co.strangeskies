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

/**
 * <p>
 * A basic interface for mutable {@link Expression} implementations.
 * Implementing classes are responsible for making sure write locks are held for
 * mutating operations, and for notifications to {@link Observer}s.
 * 
 * <p>
 * A mutating operation is considered to be any method or section of code which
 * can be considered to atomically result in a change in the value of this
 * {@link Expression}.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the value of this expression.
 */
public interface MutableExpression<T> extends Expression<T> {
	/**
	 * @return A write lock which must be obtained before attempting to mutate
	 *         this {@link Expression}.
	 */
	public Lock getWriteLock();
}
