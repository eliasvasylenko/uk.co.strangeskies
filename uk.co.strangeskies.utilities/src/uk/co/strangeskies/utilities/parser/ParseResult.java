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
package uk.co.strangeskies.utilities.parser;

import java.util.function.Function;

public class ParseResult<T> {
	private final int fromIndex;
	private final ParseState state;
	private final T result;

	ParseResult(ParseState state, int toIndex, T result) {
		this(state.fromIndex(), new ParseState(state.literal(), toIndex,
				state.toEnd()), result);
	}

	ParseResult(int fromIndex, ParseState state, T result) {
		if (state.toEnd() && state.fromIndex() < state.literal().length())
			throw new ParseState(state.literal(), fromIndex, state.toEnd())
					.addException("Cannot match literal as end of input not reached",
							state.fromIndex()).getException();

		this.fromIndex = fromIndex;
		this.state = state;
		this.result = result;
	}

	ParseState state() {
		return state;
	}

	T result() {
		return result;
	}

	<U> ParseResult<U> map(Function<? super T, ? extends U> transform) {
		return new ParseResult<U>(fromIndex, state, transform.apply(result));
	}

	public ParseResult<T> toEnd(boolean toEnd) {
		return new ParseResult<>(fromIndex, state.toEnd(toEnd), result);
	}

	public ParseResult<T> addException(ParseState state) {
		return new ParseResult<>(fromIndex, this.state.addException(state), result);
	}
}
