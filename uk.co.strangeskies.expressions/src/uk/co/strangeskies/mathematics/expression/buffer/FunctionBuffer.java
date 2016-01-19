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

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.IdentityExpression;

public class FunctionBuffer<B, F> extends IdentityExpression<F> implements
		DoubleBuffer<B, F> {
	private IdentityExpression<B> back;

	private boolean isFlat;

	private final BiFunction<? super F, ? super B, ? extends F> operation;

	public FunctionBuffer(F front, B back,
			BiFunction<? super F, ? super B, ? extends F> operation) {
		setFront(front);
		setBack(back);

		this.operation = operation;
	}

	public FunctionBuffer(F front, B back,
			Function<? super B, ? extends F> function) {
		this(front, back, (a, b) -> function.apply(b));
	}

	public FunctionBuffer(B back, Function<? super B, ? extends F> function) {
		this(function.apply(back), back, function);
	}

	public FunctionBuffer(FunctionBuffer<B, F> doubleBuffer) {
		this(doubleBuffer.getFront(), doubleBuffer.getBack(), doubleBuffer
				.getOperation());
	}

	@Override
	public Expression<B> getBackExpression() {
		return back;
	}

	public BiFunction<? super F, ? super B, ? extends F> getOperation() {
		return operation;
	}

	@Override
	public F setFront(F front) {
		isFlat = false;

		return set(front);
	}

	@Override
	public B setBack(B back) {
		isFlat = false;

		return this.back.set(back);
	}

	@Override
	public F getFront() {
		return get();
	}

	@Override
	public B getBack() {
		return back.get();
	}

	@Override
	public void push() {
		if (!isFlat) {
			setFront(getOperation().apply(getFront(), getBack()));
			isFlat = true;
		}
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof FunctionBuffer<?, ?>)) {
			return false;
		}

		DoubleBuffer<?, ?> thatDoubleBuffer = (DoubleBuffer<?, ?>) that;

		F thisFront = this.getFront();
		B thisBack = this.getBack();
		Object thatFront = thatDoubleBuffer.getFront();
		Object thatBack = thatDoubleBuffer.getBack();

		return Objects.equals(thisFront, thatFront)
				&& Objects.equals(thisBack, thatBack);
	}

	@Override
	public int hashCode() {
		int hashCode = getFront().hashCode() + getBack().hashCode() * 29;

		return hashCode;
	}

	@Override
	public boolean isFlat() {
		return isFlat;
	}

	@Override
	public String toString() {
		return "[" + getBack() + "] => [" + getFront() + "]";
	}
}
