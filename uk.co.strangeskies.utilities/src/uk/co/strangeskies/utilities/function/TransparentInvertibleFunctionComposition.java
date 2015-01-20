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

import java.util.function.Function;

public class TransparentInvertibleFunctionComposition<T, I, R> implements
		Function<T, R>, InvertibleFunction<T, R> {
	private InvertibleFunction<T, I> firstFunction;
	private InvertibleFunction<I, R> secondFunction;
	private I intermediateResult;

	private TransparentInvertibleFunctionComposition<R, I, T> inverse;

	public TransparentInvertibleFunctionComposition(
			InvertibleFunction<T, I> firstFunction,
			InvertibleFunction<I, R> secondFunction) {
		this.firstFunction = firstFunction;
		this.secondFunction = secondFunction;

		inverse = new TransparentInvertibleFunctionComposition<R, I, T>(this);
	}

	protected TransparentInvertibleFunctionComposition(
			TransparentInvertibleFunctionComposition<R, I, T> inverse) {
		this.inverse = inverse;

		firstFunction = inverse.getSecondFunction().getInverse();
		secondFunction = inverse.getFirstFunction().getInverse();
	}

	@Override
	public R apply(T input) {
		intermediateResult = firstFunction.apply(input);
		return secondFunction.apply(intermediateResult);
	}

	public I getIntermediateResult() {
		return intermediateResult;
	}

	public InvertibleFunction<T, I> getFirstFunction() {
		return firstFunction;
	}

	public InvertibleFunction<I, R> getSecondFunction() {
		return secondFunction;
	}

	@Override
	public TransparentInvertibleFunctionComposition<R, I, T> getInverse() {
		return inverse;
	}
}
