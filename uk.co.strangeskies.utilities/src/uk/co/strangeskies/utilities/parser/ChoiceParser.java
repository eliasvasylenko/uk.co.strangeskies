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
import java.util.function.Supplier;

public class ChoiceParser<U, T> extends ParserProxy<U, T> {
	private final Parser<? extends U> onFailure;

	public ChoiceParser(Supplier<Parser<U>> component,
			Parser<? extends U> onFailure, Function<? super U, ? extends T> transform) {
		super(component, transform);

		this.onFailure = onFailure;
	}

	@Override
	public ParseResult<T> parseSubstring(ParseState state) {
		System.out.println(getClass());

		ParseResult<T> value;
		try {
			value = super.parseSubstring(state);
		} catch (ParsingException e) {
			try {
				value = onFailure.transform(getTransform()).parseSubstring(state)
						.map(Function.identity());
			} catch (ParsingException e2) {
				throw ParsingException.getHigher(e, e2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			value = onFailure.transform(getTransform()).parseSubstring(state)
					.map(Function.identity());
		}
		return value;
	}

	@Override
	public <V> Parser<V> transform(Function<? super T, ? extends V> transform) {
		return new ChoiceParser<>(getComponent(), onFailure, getTransform()
				.andThen(transform));
	}
}
