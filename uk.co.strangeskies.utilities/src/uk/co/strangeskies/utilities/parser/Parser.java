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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.tuples.Pair;

public interface Parser<T> {
	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter) {
		return list(element, delimiter, 0);
	}

	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter,
			int minimum) {
		IdentityProperty<Parser<List<T>>> listParser = new IdentityProperty<>();

		listParser.set(
				Parser.proxy(listParser::get).prepend(delimiter).orElse(ArrayList::new)
						.prepend(element, List::add)).orElse(ArrayList::new);

		for (int i = 0; i < minimum; i++)
			listParser.set(listParser.get().prepend(delimiter)
					.prepend(element, List::add));

		return listParser.get();
	}

	public static Parser<String> matching(String regex) {
		return new RegexParser<>(regex, Function.identity());
	}

	public static <T> Parser<T> from(Supplier<T> supplier) {
		return new RegexParser<>("", s -> supplier.get());
	}

	public static <T> Parser<T> proxy(Supplier<Parser<T>> parser) {
		return new ParserProxy<>(parser, Function.identity());
	}

	<U> Parser<U> transform(Function<T, U> transform);

	Parser<T> orElse(Supplier<T> onFailure);

	Parser<T> orElse(Parser<T> onFailure);

	default Parser<T> append(String pattern) {
		return append(pattern, (t, s) -> {});
	}

	<U> Parser<U> appendTransform(String pattern,
			BiFunction<T, String, U> incorporate);

	default Parser<T> append(String pattern, BiConsumer<T, String> incorporate) {
		return appendTransform(pattern, (t, s) -> {
			incorporate.accept(t, s);
			return t;
		});
	}

	default Parser<T> prepend(String pattern) {
		return prepend(pattern, (s, t) -> {});
	}

	<U> Parser<U> prependTransform(String pattern,
			BiFunction<T, String, U> incorporate);

	default Parser<T> prepend(String pattern, BiConsumer<T, String> incorporate) {
		return prependTransform(pattern, (t, s) -> {
			incorporate.accept(t, s);
			return t;
		});
	}

	<U, V> Parser<V> appendTransform(Parser<U> parser,
			BiFunction<T, U, V> incorporate);

	default <U> Parser<T> append(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return appendTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U> Parser<T> tryAppendTransform(Parser<U> parser,
			BiFunction<T, U, T> incorporate);

	default <U> Parser<T> tryAppend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return tryAppendTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U, V> Parser<V> prependTransform(Parser<U> parser,
			BiFunction<T, U, V> incorporate);

	default <U> Parser<T> prepend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return prependTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U> Parser<T> tryPrependTransform(Parser<U> parser,
			BiFunction<T, U, T> incorporate);

	default <U> Parser<T> tryPrepend(Parser<U> parser,
			BiConsumer<T, U> incorporate) {
		return tryPrependTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	default T parse(String literal) {
		return parse(literal, true);
	}

	default T parse(String literal, boolean parseToEnd) {
		return parseSubstring(literal, true).getHead();
	}

	default Pair<T, Integer> parseSubstring(String literal) {
		return parseSubstring(literal, false);
	}

	Pair<T, Integer> parseSubstring(String literal, boolean parseToEnd);
}
