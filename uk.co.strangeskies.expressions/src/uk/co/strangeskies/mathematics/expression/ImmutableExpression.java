/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.mathematics.expression;

import java.util.function.Consumer;

/**
 * An abstract class to help designing mutable expression, implementing a simple
 * observer list, locking mechanism, and update mechanism.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public abstract class ImmutableExpression<S extends Expression<S, T>, T> implements Expression<S, T> {
	@Override
	public final boolean addObserver(Consumer<? super S> observer) {
		return true;
	}

	@Override
	public final boolean removeExactObserver(Consumer<? super S> observer) {
		return true;
	}
}
