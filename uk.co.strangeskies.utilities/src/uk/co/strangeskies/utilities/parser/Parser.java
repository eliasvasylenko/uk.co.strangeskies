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

/**
 * A composable, type safe text parser.
 * <p>
 * Usage should be intuitive enough for users familiar with context free
 * grammars, where a {@link Parser} instance represents a single symbol, and all
 * production rules on that symbol. {@link Parser} instances should be
 * immutable.
 * <p>
 * By this interpretation, production rules for new symbols can be derived by
 * appending and prepending text, or other symbols, to existing symbols.
 * Multiple production rules can effectively be created for a new symbol by
 * "piping" production rules through {@link #orElse(Parser)} and
 * {@link #orElse(Supplier)}.
 * <p>
 * For the sake of simplicity, all parsers are composed and evaluated greedily
 * from left to right, unless otherwise noted.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the object created by successful application of a
 *          {@link Parser} to a piece of text.
 */
public interface Parser<T> {
	/**
	 * Convenience method to create a {@link Parser} which accepts a list of text
	 * items, delimited by the given string, which are each parsable according to
	 * the given parser.
	 * 
	 * @param <T>
	 *          The type of the objects to be parsed as elements of the list.
	 * @param element
	 *          The parser responsible for parsing the elements of the list.
	 * @param delimiter
	 *          The string delimiter between parsable text items in the list. This
	 *          may be set to an empty string.
	 * @return A newly derived {@link Parser} which will accept a {@link String}
	 *         representation of a list of items, and parse it into a {@link List}
	 *         instance containing objects parsed from its elements.
	 */
	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter) {
		return list(element, delimiter, 0);
	}

	/**
	 * Convenience method to create a {@link Parser} which accepts a list of text
	 * items, delimited by the given string, which are each parsable according to
	 * the given parser.
	 * 
	 * @param <T>
	 *          The type of the objects to be parsed as elements of the list.
	 * @param element
	 *          The parser responsible for parsing the elements of the list.
	 * @param delimiter
	 *          The string delimiter between parsable text items in the list. This
	 *          may be set to an empty string.
	 * @param minimum
	 *          The minimum length of the list to be parsed.
	 * @return A newly derived {@link Parser} which will accept a {@link String}
	 *         representation of a list of items, and parse it into a {@link List}
	 *         instance containing objects parsed from its elements.
	 */
	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter,
			int minimum) {
		IdentityProperty<Parser<List<T>>> listParser = new IdentityProperty<>();

		listParser.set(Parser.proxy(listParser::get).prepend(delimiter)
				.orElse(ArrayList::new).prepend(element, (l, e) -> l.add(0, e))
				.orElse(ArrayList::new));

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

	<U> Parser<U> transform(Function<? super T, ? extends U> transform);

	Parser<T> orElse(Supplier<? extends T> onFailure);

	Parser<T> orElse(Parser<? extends T> onFailure);

	default Parser<T> append(String pattern) {
		return append(pattern, (t, s) -> {});
	}

	<U> Parser<U> appendTransform(String pattern,
			BiFunction<T, String, ? extends U> incorporate);

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
			BiFunction<T, String, ? extends U> incorporate);

	default Parser<T> prepend(String pattern, BiConsumer<T, String> incorporate) {
		return prependTransform(pattern, (t, s) -> {
			incorporate.accept(t, s);
			return t;
		});
	}

	<U, V> Parser<V> appendTransform(Parser<U> parser,
			BiFunction<T, U, ? extends V> incorporate);

	default <U> Parser<T> append(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return appendTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U> Parser<T> tryAppendTransform(Parser<U> parser,
			BiFunction<T, U, ? extends T> incorporate);

	default <U> Parser<T> tryAppend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return tryAppendTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U, V> Parser<V> prependTransform(Parser<U> parser,
			BiFunction<T, U, ? extends V> incorporate);

	default <U> Parser<T> prepend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return prependTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	<U> Parser<T> tryPrependTransform(Parser<U> parser,
			BiFunction<T, U, ? extends T> incorporate);

	default <U> Parser<T> tryPrepend(Parser<U> parser,
			BiConsumer<T, U> incorporate) {
		return tryPrependTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	/*
	 * TODO greedy append: evaluate appended value first, not needing to match
	 * from the beginning of the unmatched region.
	 * 
	 * lazy prepend: evaluate original value first, not needing to match from the
	 * beginning of the unmatched region.
	 */

	T parse(String literal);

	ParseResult<T> parseSubstring(ParseState currentState);
}
