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
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractParser<T> implements Parser<T> {
	@Override
	public <W> Parser<W> to(Function<T, W> transform) {
		return new ParserProxy<>(this, transform);
	}

	@Override
	public Parser<T> orElse(Supplier<T> onFailure) {
		return orElse(new RegexParser<>("", s -> onFailure.get()));
	}

	@Override
	public Parser<T> orElse(Parser<T> onFailure) {
		return new ParserProxy<>(() -> this, Function.identity()).orElse(onFailure);
	}

	@Override
	public <U> Parser<U> appendTransform(String pattern,
			BiFunction<T, String, U> incorporate) {
		return new ParserJoin<U, T, String>(this, new RegexParser<String>(pattern,
				Function.identity()), incorporate);
	}

	@Override
	public <U> Parser<U> prependTransform(String pattern,
			BiFunction<T, String, U> incorporate) {
		return new ParserJoin<U, String, T>(new RegexParser<String>(pattern,
				Function.identity()), this, (s, t) -> incorporate.apply(t, s));
	}

	@Override
	public <U, V> Parser<V> appendTransform(Parser<U> parser,
			BiFunction<T, U, V> incorporate) {
		return new ParserJoin<V, T, U>(this, parser, incorporate);
	}

	@Override
	public <U, V> Parser<V> prependTransform(Parser<U> parser,
			BiFunction<T, U, V> incorporate) {
		return new ParserJoin<V, U, T>(parser, this, (v, t) -> incorporate.apply(t,
				v));
	}
}
