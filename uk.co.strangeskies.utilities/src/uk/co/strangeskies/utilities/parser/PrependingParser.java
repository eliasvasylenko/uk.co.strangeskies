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

import java.util.function.BiFunction;

import uk.co.strangeskies.utilities.tuples.Pair;

public class PrependingParser<T, U> extends AbstractParser<T> {
	private final Parser<T> main;
	private final Parser<U> prepend;

	private final BiFunction<T, U, ? extends T> combinor;

	public PrependingParser(Parser<T> main, Parser<U> prepend,
			BiFunction<T, U, ? extends T> combinor) {
		this.main = main;
		this.prepend = prepend;

		this.combinor = combinor;
	}

	@Override
	public Pair<T, Integer> parseSubstring(String literal, boolean parseToEnd) {
		Pair<T, Integer> mainValue;
		Pair<U, Integer> prependValue;

		try {
			prependValue = prepend.parseSubstring(literal);
			literal = literal.substring(prependValue.getRight());
		} catch (Exception e) {
			return main.parseSubstring(literal);
		}

		mainValue = main.parseSubstring(literal);

		return new Pair<>(combinor.apply(mainValue.getLeft(),
				prependValue.getLeft()), mainValue.getRight() + prependValue.getRight());
	}

	@Override
	public String toString() {
		return "Prepending Parser [" + prepend + " > " + main + "]";
	}
}
