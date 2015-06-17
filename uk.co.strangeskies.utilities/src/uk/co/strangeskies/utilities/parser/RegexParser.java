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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.strangeskies.utilities.tuples.Pair;

public class RegexParser<T> extends AbstractParser<T> {
	private final Pattern pattern;
	private final Function<String, T> transform;

	public RegexParser(String pattern, Function<String, T> transform) {
		this.pattern = Pattern.compile("\\A" + pattern);
		this.transform = transform;
	}

	@Override
	public Pair<T, Integer> parseSubstring(String literal) {
		Matcher matcher = pattern.matcher(literal);

		if (!matcher.find()) {
			throw new IllegalStateException("Cannot match literal '" + literal
					+ "' to pattern '" + pattern + "' in parser '" + this + "'");
		}

		int end = matcher.end();
		System.out.println(literal + " > " + this);
		literal = literal.substring(0, end);
		System.out.println(literal);
		return new Pair<>(transform.apply(literal), end);
	}

	@Override
	public String toString() {
		return "Regular Expression Parser " + pattern;
	}
}
