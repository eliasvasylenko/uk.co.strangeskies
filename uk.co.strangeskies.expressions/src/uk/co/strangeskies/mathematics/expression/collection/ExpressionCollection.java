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

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.SelfExpression;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.ObservableCollection;

public interface ExpressionCollection<S extends ExpressionCollection<S, E, C>, E extends Expression<?, ?>, C>
		extends ObservableCollection<S, E, C>, Self<S>, SelfExpression<S> {
	@Override
	ExpressionCollection<?, E, ?> unmodifiableView();

	@Override
	ExpressionCollection<?, E, ?> synchronizedView();

	void set(Collection<? extends E> expressions);
}
