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

import uk.co.strangeskies.utilities.ObservableImpl;

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
public abstract class ExpressionImpl<S extends Expression<S, T>, T> extends ObservableImpl<S>
		implements Expression<S, T> {
	private boolean dirty;

	private boolean changing;
	private int changeDepth = 0;

	protected void cancelChange() {
		changing = false;
	}

	protected boolean isChanging() {
		return changing;
	}

	protected void write(Runnable runnable) {
		beginWrite();

		try {
			runnable.run();
		} finally {
			endWrite();
		}
	}

	protected boolean beginWrite() {
		boolean begun = changeDepth++ == 0;

		if (begun) {
			changing = getObservers().size() > 0;
		}

		return begun;
	}

	protected boolean endWrite() {
		boolean ended = --changeDepth == 0;

		if (ended && changing) {
			fireChange();
		}

		return ended;
	}

	private boolean fireChange() {
		boolean fired = !dirty;

		if (fired) {
			dirty = true;
			fire(getThis());
		}

		return fired;
	}

	@Override
	public T getValue() {
		boolean dirty = this.dirty;

		if (this.dirty) {
			this.dirty = false;
		}

		return getValueImpl(dirty);
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
}
