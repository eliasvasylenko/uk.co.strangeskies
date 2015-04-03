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

/**
 * Describes a function from F to T. A function should be stateless.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          Operand type.
 * @param <R>
 *          Result type.
 */
public interface InvertibleFunction<T, R> extends Function<T, R> {
	/**
	 * This returns the mathematical inverse of the receiving function.
	 * 
	 * @return A new Invertible function performing the inverse operation.
	 */
	public InvertibleFunction<R, T> getInverse();

	/**
	 * @param function
	 * @param reverse
	 * @return An invertible function using the two given functions.
	 */
	public static <T, R> InvertibleFunction<T, R> over(
			Function<? super T, ? extends R> function,
			Function<? super R, ? extends T> reverse) {
		return new InvertibleFunction<T, R>() {
			@Override
			public R apply(T t) {
				return function.apply(t);
			}

			@Override
			public InvertibleFunction<R, T> getInverse() {
				return over(reverse, function);
			}
		};
	}

	/**
	 * @param first
	 * @param second
	 * @return Composition of two invertible functions into a single invertible
	 *         function.
	 */
	public static <T, I, R> InvertibleFunction<T, R> compose(
			InvertibleFunction<T, I> first, InvertibleFunction<I, R> second) {
		return over(first.andThen(second),
				second.getInverse().andThen(first.getInverse()));
	}
}
