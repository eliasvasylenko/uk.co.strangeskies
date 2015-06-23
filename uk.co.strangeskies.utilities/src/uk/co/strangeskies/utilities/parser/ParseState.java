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

public class ParseState {
	private final ParsingException furthestException;

	private final String literal;
	private final int fromIndex;
	private final boolean toEnd;

	public ParseState(String literal) {
		this(literal, 0, true);
	}

	public ParseState(String literal, int fromIndex, boolean toEnd) {
		this.literal = literal;
		this.fromIndex = fromIndex;
		this.toEnd = toEnd;
		this.furthestException = null;
	}

	public ParseState(ParseState state, ParsingException furthestException) {
		this.literal = state.literal;
		this.fromIndex = state.fromIndex;
		this.toEnd = state.toEnd;
		this.furthestException = furthestException;
	}

	public String literal() {
		return literal;
	}

	public int fromIndex() {
		return fromIndex;
	}

	public boolean toEnd() {
		return toEnd;
	}

	public ParseState toEnd(boolean toEnd) {
		return new ParseState(literal, fromIndex, toEnd);
	}

	public <T> ParseResult<T> parseTo(int toIndex, Function<String, T> transform) {
		return new ParseResult<T>(this, toIndex, transform.apply(literal.substring(
				fromIndex, toIndex)));
	}

	public ParsingException getException() {
		return furthestException;
	}

	public ParseState addException(ParsingException exception) {
		if (furthestException != null
				&& exception.getIndexReached() < furthestException.getIndexReached())
			exception = furthestException;

		return new ParseState(this, exception);
	}

	public ParseState addException(String message, int indexReached) {
		return addException(new ParsingException(message, literal, fromIndex,
				indexReached, furthestException));
	}

	public ParseState addException(ParseState state) {
		return addException(state.getException());
	}
}
