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

public class PrependingParser<T, U> implements AbstractParser<T> {
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
	public ParseResult<T> parseSubstring(ParseState state) {
		System.out.println(getClass());

		ParseResult<U> prependValue;
		ParseResult<T> mainValue;

		try {
			prependValue = prepend.parseSubstring(state.toEnd(false));
		} catch (ParsingException e) {
			try {
				return main.parseSubstring(state);
			} catch (ParsingException e2) {
				throw ParsingException.getHigher(e, e2);
			} catch (Exception e2) {
				throw e;
			}
		} catch (Exception e) {
			return main.parseSubstring(state);
		}

		mainValue = main.parseSubstring(prependValue.state().toEnd(state.toEnd()));

		return mainValue.map(m -> combinor.apply(m, prependValue.result()));
	}

	@Override
	public String toString() {
		return "Prepending Parser [" + prepend + " > " + main + "]";
	}
}
