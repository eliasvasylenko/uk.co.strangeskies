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
package uk.co.strangeskies.mathematics.expression.collection;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.collection.ObservableListDecorator;

public abstract class ExpressionListDecorator<E extends Expression<?, ?>> extends
		ObservableListDecorator<ExpressionListDecorator<E>, E> implements ExpressionList<ExpressionListDecorator<E>, E>,
		CopyDecouplingExpression<ExpressionListDecorator<E>, ExpressionListDecorator<E>> {
	private boolean evaluated;

	private final Consumer<Expression<?, ?>> dependencyObserver;

	public ExpressionListDecorator(List<E> component) {
		super(component);

		dependencyObserver = message -> fireEvent();
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
				((Expression<?, ?>) expression).removeObserver(dependencyObserver);
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
	public void clear() {
		try {
			beginChange();

			for (E e : this) {
				e.removeObserver(dependencyObserver);
			}

			super.clear();
		} finally {
			endChange();
		}
	}

	@Override
	public ExpressionList<?, E> synchronizedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final ExpressionList<?, E> unmodifiableView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final ExpressionListDecorator<E> getValue() {
		evaluated = true;

		return this;
	}
}
