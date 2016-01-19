/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.text;


public class ParseException extends RuntimeException {
	private final String message;
	private final String literal;
	private final int fromIndex;
	private final int toIndex;

	public ParseException(String message, String literal, int fromIndex,
			int toIndex, Throwable cause) {
		super(composeMessage(message, literal, fromIndex, toIndex), cause);
		this.message = message;
		this.literal = literal;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	public ParseException(String message, String literal, int fromIndex,
			int toIndex) {
		super(composeMessage(message, literal, fromIndex, toIndex));
		this.message = message;
		this.literal = literal;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	private static String composeMessage(String message, String literal,
			int fromIndex, int toIndex) {
		StringBuilder builder = new StringBuilder(message)
				.append(System.lineSeparator()).append(literal)
				.append(System.lineSeparator());

		for (int i = 0; i < fromIndex; i++)
			builder.append(" ");
		builder.append("^");

		for (int i = fromIndex; i < toIndex; i++)
			builder.append(".");
		builder.append("^");

		return builder.toString();
	}

	public String getLiteral() {
		return literal;
	}

	public int getStartIndex() {
		return fromIndex;
	}

	public int getIndexReached() {
		return toIndex;
	}

	public String getSimpleMessage() {
		return message;
	}

	public static ParseException getHigher(ParseException first,
			ParseException second) {
		if (first.getIndexReached() > second.getIndexReached())
			return first;
		else
			return second;
	}
}
