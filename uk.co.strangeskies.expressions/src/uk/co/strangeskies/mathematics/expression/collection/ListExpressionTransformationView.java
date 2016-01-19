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

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.Expression;

/**
 * A view of a list which will be automatically updated along with the original,
 * but who's elements will be a transformation of the original associated
 * elements by way of the function passed to the constructor. The implementation
 * employs lazy evaluation, so try to use get() as little as possible by reusing
 * the result.
 *
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the elements of this list.
 * @param <F>
 *          The type of the elements of the backing list.
 */
public class ListExpressionTransformationView<F, T> extends AbstractList<T> {
	private final Expression<? extends List<? extends F>> backingList;
	private final Expression<? extends Function<? super F, ? extends T>> function;

	public ListExpressionTransformationView(
			Expression<? extends List<? extends F>> backingList,
			Expression<? extends Function<? super F, ? extends T>> function) {
		this.backingList = backingList;
		this.function = function;
	}

	@Override
	public final T get(int index) {
		return function.getValue().apply(backingList.getValue().get(index));
	}

	public final List<F> getBackingList() {
		return new ListExpressionView<F>(backingList);
	}

	public final Expression<? extends Function<? super F, ? extends T>> getFunction() {
		return function;
	}

	@Override
	public final int size() {
		return backingList.getValue().size();
	}
}
