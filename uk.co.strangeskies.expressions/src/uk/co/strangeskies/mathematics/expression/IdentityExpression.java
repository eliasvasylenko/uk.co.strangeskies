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

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

/**
 * An {@link Expression} based on the behaviour of the {@link IdentityProperty}
 * class, with the lazy updating behaviour of {@link MutableExpressionImpl} for
 * {@link Observer}s.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public class IdentityExpression<T> extends MutableExpressionImpl<T> implements
		Property<T, T> {
	private T value;

	/**
	 * Construct with a default value of {@code null}.
	 */
	public IdentityExpression() {}

	/**
	 * Construct with the given default value.
	 * 
	 * @param value
	 *          The initial value of the expression.
	 */
	public IdentityExpression(T value) {
		this.value = value;
	}

	@Override
	public T set(T value) {
		try {
			getWriteLock().lock();
			T previous = this.value;
			this.value = value;
			return previous;
		} finally {
			postUpdate();
		}
	}

	@Override
	protected final T getValueImpl(boolean dirty) {
		return value;
	}

	@Override
	public final T get() {
		return getValue();
	}
}
