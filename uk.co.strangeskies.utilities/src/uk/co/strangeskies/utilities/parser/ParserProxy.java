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

public class ParserProxy<U, T> implements AbstractParser<T> {
	private final Supplier<Parser<U>> component;
	private final Function<? super U, ? extends T> transform;

	public ParserProxy(Parser<U> component,
			Function<? super U, ? extends T> transform) {
		this(() -> component, transform);
	}

	public ParserProxy(Supplier<Parser<U>> component,
			Function<? super U, ? extends T> transform) {
		this.component = component;
		this.transform = transform;
	}

	protected Supplier<Parser<U>> getComponent() {
		return component;
	}

	public Function<? super U, ? extends T> getTransform() {
		return transform;
	}

	@Override
	public <V> Parser<V> transform(Function<? super T, ? extends V> transform) {
		return new ParserProxy<>(component, this.transform.andThen(transform));
	}

	@Override
	public ParseResult<T> parseSubstring(ParseState state) {
		return component.get().parseSubstring(state).map(transform);
	}

	@Override
	public String toString() {
		return "Proxy Parser (" + component + ")";
	}
}
