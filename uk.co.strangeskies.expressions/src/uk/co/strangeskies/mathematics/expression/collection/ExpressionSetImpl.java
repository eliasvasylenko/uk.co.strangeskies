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
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.collection.ObservableSetDecorator;

public class ExpressionSetImpl<E extends Expression<?, ?>> extends ObservableSetDecorator<ExpressionSetImpl<E>, E>
		implements SortedExpressionSet<ExpressionSetImpl<E>, E>,
		CopyDecouplingExpression<ExpressionSetImpl<E>, ExpressionSetImpl<E>> {
	private static final long serialVersionUID = 1L;

	private boolean evaluated = true;

	private Consumer<Expression<?, ?>> dependencyObserver = e -> fireEvent();

	public ExpressionSetImpl(Set<E> component) {
		super(component);
	}

	@Override
	protected void fireEvent() {
		if (evaluated) {
			evaluated = false;
			super.fireEvent();
		}
	}

	@Override
	public final boolean add(E expression) {
		try {
			beginChange();

			boolean added = super.add(expression);

			if (added) {
				expression.addWeakObserver(dependencyObserver);
			}

			return added;
		} finally {
			endChange();
		}
	}

	@Override
	public final boolean remove(Object expression) {
		try {
			beginChange();

			boolean added = super.remove(expression);

			if (added) {
				((Expression<?, ?>) expression).removeWeakObserver(dependencyObserver);
			}

			return added;
		} finally {
			endChange();
		}
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		try {
			beginChange();

			retainAll(expressions);
			addAll(expressions);
		} finally {
			endChange();
		}
	}

	@Override
	public ExpressionSet<?, E> synchronizedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final ExpressionSet<?, E> unmodifiableView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final ExpressionSetImpl<E> getValue() {
		evaluated = true;

		return this;
	}

	@Override
	public final ExpressionSetImpl<E> copy() {
		return new ExpressionSetImpl<>(this);
	}
}
