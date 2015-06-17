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

import uk.co.strangeskies.utilities.tuples.Pair;

public class ParserProxy<U, T> extends AbstractParser<T> {
	private final Supplier<Parser<U>> component;
	private final Function<U, T> transform;

	public ParserProxy(Parser<U> component, Function<U, T> transform) {
		this(() -> component, transform);
	}

	public ParserProxy(Supplier<Parser<U>> component, Function<U, T> transform) {
		this.component = component;
		this.transform = transform;
	}

	@Override
	public <V> Parser<V> to(Function<T, V> transform) {
		return new ParserProxy<>(component, this.transform.andThen(transform));
	}

	@Override
	public Parser<T> orElse(Parser<T> onFailure) {
		return new ParserProxy<U, T>(component, transform) {
			@Override
			public Pair<T, Integer> parseSubstring(String literal) {
				Pair<T, Integer> value;
				try {
					value = super.parseSubstring(literal);
				} catch (Exception e) {
					value = onFailure.parseSubstring(literal);
				}
				return value;
			}
		};
	}

	@Override
	public Pair<T, Integer> parseSubstring(String literal) {
		return component.get().parseSubstring(literal).mapHead(transform);
	}

	@Override
	public String toString() {
		return "Proxy Parser (" + component + ")";
	}
}
