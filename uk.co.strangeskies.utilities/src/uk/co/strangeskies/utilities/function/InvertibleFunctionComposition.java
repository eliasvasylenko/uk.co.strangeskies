/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.function;

import uk.co.strangeskies.utilities.Decorator;

public class InvertibleFunctionComposition<T, R> extends
		Decorator<TransparentInvertibleFunctionComposition<T, ?, R>> implements
		InvertibleFunction<T, R> {
	private InvertibleFunctionComposition<R, T> inverse;

	public <I> InvertibleFunctionComposition(
			InvertibleFunction<T, I> firstFunction,
			InvertibleFunction<I, R> secondFunction) {
		super(new TransparentInvertibleFunctionComposition<>(firstFunction,
				secondFunction));
	}

	protected InvertibleFunctionComposition(
			InvertibleFunctionComposition<R, T> inverse) {
		super(inverse.getComponent().getInverse());

		this.inverse = inverse;
	}

	@Override
	public R apply(T input) {
		return getComponent().apply(input);
	}

	@Override
	public InvertibleFunctionComposition<R, T> getInverse() {
		return inverse;
	}
}
