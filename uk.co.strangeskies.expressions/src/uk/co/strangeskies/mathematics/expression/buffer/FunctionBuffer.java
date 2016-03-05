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
package uk.co.strangeskies.mathematics.expression.buffer;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionBuffer<B, F> extends AbstractFunctionBuffer<FunctionBuffer<B, F>, B, F> {
	public FunctionBuffer(F front, B back, BiFunction<? super F, ? super B, ? extends F> operation) {
		super(front, back, operation);
	}

	public FunctionBuffer(F front, B back, Function<? super B, ? extends F> function) {
		super(front, back, function);
	}

	public FunctionBuffer(B back, Function<? super B, ? extends F> function) {
		super(back, function);
	}

	public FunctionBuffer(FunctionBuffer<B, F> doubleBuffer) {
		super(doubleBuffer);
	}

	@Override
	public FunctionBuffer<B, F> copy() {
		return new FunctionBuffer<>(this);
	}
}
