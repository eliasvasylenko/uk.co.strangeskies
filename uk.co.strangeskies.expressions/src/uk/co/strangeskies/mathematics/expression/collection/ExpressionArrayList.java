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

	public ExpressionArrayList() {
		dependencyObserver = message -> fireEvent();
	}

	public ExpressionArrayList(Collection<E> expressions) {
		this();

		addAll(expressions);
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
			beginChange();

			super.add(expression);
			expression.addObserver(dependencyObserver);

			return true;
		} finally {
			endChange();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		try {
			beginChange();

			boolean removed = super.remove(expression);
			if (removed) {
				((E) expression).removeObserver(dependencyObserver);
			}

			return removed;
		} finally {
			endChange();
		}
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		try {
			beginChange();

			boolean changed = false;
			for (E expression : expressions)
				if (super.add(expression)) {
					expression.addObserver(dependencyObserver);
					changed = true;
				}

			return changed;
		} finally {
			endChange();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		try {
			beginChange();

			boolean changed = false;
			for (Object expression : expressions)
				if (super.remove(expression)) {
					((E) expression).removeObserver(dependencyObserver);
					changed = true;
				}

			return changed;
		} finally {
			endChange();
		}
	}

	@Override
	public final void clear() {
		try {
			beginChange();
			if (!isEmpty()) {

				for (E expression : this)
					expression.removeObserver(dependencyObserver);

				super.clear();
			}
		} finally {
			endChange();
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
			beginChange();

			for (E expression : this)
				if (!c.contains(expression))
					expression.removeObserver(dependencyObserver);

			return super.retainAll(c);
		} finally {
			endChange();
		}
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableList(this);
	}

	@Override
	public final ExpressionArrayList<E> getValue() {
		try {
			beginChange();
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
			beginChange();
			return new ExpressionArrayList<>(this);
		} finally {
			endChange();
		}
	}

	@Override
	public final void add(int index, E expression) {
		try {
			beginChange();

			super.add(index, expression);

			expression.addObserver(dependencyObserver);
		} finally {
			endChange();
		}
	}

	@Override
	public final boolean addAll(int index, Collection<? extends E> expressions) {
		try {
			beginChange();

			for (E expression : expressions) {
				expression.addObserver(dependencyObserver);
			}
			super.addAll(index, expressions);
		} finally {
			endChange();
		}

		return !expressions.isEmpty();
	}

	@Override
	public final E remove(int index) {
		try {
			beginChange();

			E removed = super.remove(index);

			removed.removeObserver(dependencyObserver);

			return removed;
		} finally {
			endChange();
		}
	}

	@Override
	public final E set(int index, E expression) {
		try {
			beginChange();

			E previous = super.set(index, expression);

			previous.removeObserver(dependencyObserver);
			expression.addObserver(dependencyObserver);

			return previous;
		} finally {
			endChange();
		}
	}
}
