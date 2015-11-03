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

import java.util.Collection;
import java.util.Iterator;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.SelfExpression;
import uk.co.strangeskies.utilities.Self;

public interface ExpressionCollection<S extends ExpressionCollection<S, E>, E extends Expression<?>>
		extends Collection<E>, Self<S>, SelfExpression<S> {
	@Override
	public boolean contains(Object o);

	@Override
	public boolean containsAll(Collection<?> c);

	@Override
	public boolean isEmpty();

	@Override
	public Iterator<E> iterator();

	@Override
	public int size();

	@Override
	public Object[] toArray();

	@Override
	public <A> A[] toArray(A[] a);

	public Collection<E> getUnmodifiableView();

	public void set(Collection<? extends E> expressions);
}
