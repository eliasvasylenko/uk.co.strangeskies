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

public interface AbstractParser<T> extends Parser<T> {
	@Override
	default <W> Parser<W> transform(Function<? super T, ? extends W> transform) {
		return new ParserProxy<>(this, transform);
	}

	@Override
	default Parser<T> orElse(Supplier<? extends T> onFailure) {
		return orElse(new RegexParser<>("", s -> onFailure.get()));
	}

	@Override
	default Parser<T> orElse(Parser<? extends T> onFailure) {
		return new ChoiceParser<T, T>(() -> this, onFailure, Function.identity());
	}

	@Override
	default <U> Parser<U> appendTransform(String pattern,
			BiFunction<T, String, ? extends U> incorporate) {
		return new JoiningParser<>(this, new RegexParser<String>(pattern,
				Function.identity()), incorporate);
	}

	@Override
	default <U> Parser<U> prependTransform(String pattern,
			BiFunction<T, String, ? extends U> incorporate) {
		return new JoiningParser<>(new RegexParser<String>(pattern,
				Function.identity()), this, (s, t) -> incorporate.apply(t, s));
	}

	@Override
	default <U, V> Parser<V> appendTransform(Parser<U> parser,
			BiFunction<T, U, ? extends V> incorporate) {
		return new JoiningParser<>(this, parser, incorporate);
	}

	@Override
	default <U, V> Parser<V> prependTransform(Parser<U> parser,
			BiFunction<T, U, ? extends V> incorporate) {
		return new JoiningParser<>(parser, this, (v, t) -> incorporate.apply(t, v));
	}

	@Override
	default <U> Parser<T> tryAppendTransform(Parser<U> parser,
			BiFunction<T, U, ? extends T> incorporate) {
		return new AppendingParser<>(this, parser, incorporate);
	}

	@Override
	default <U> Parser<T> tryPrependTransform(Parser<U> parser,
			BiFunction<T, U, ? extends T> incorporate) {
		return new PrependingParser<>(this, parser, incorporate);
	}

	@Override
	default T parse(String literal) {
		return parseSubstring(new ParseState(literal)).result();
	}

	@Override
	default ParseResult<T> parseSubstring(ParseState currentState) {
		return parseSubstringImpl(currentState.push(this)).mapState(
				s -> s.pop(this));
	}

	ParseResult<T> parseSubstringImpl(ParseState currentState);
}
