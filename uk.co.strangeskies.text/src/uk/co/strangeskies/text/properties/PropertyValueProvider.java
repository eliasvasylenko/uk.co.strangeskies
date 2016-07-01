/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.text.parsing.Parser;

/**
 * A provider of {@link PropertyValue property values} for properties of a
 * certain class.
 * <p>
 * In most use cases the simplest {@link #over(Class, Parser)} method can be
 * used to generate instances over a given value class and property parser.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the property value
 */
public interface PropertyValueProvider<T> {
	Parser<PropertyValue<T>> getParser();

	default Optional<PropertyValue<T>> getDefault(String keyString) {
		return empty();
	}

	static <C, T> PropertyValueProvider<T> over(Parser<C> getValue, BiFunction<C, List<?>, T> instantiate,
			Function<String, C> defaultValue) {
		return new PropertyValueProvider<T>() {
			@Override
			public Parser<PropertyValue<T>> getParser() {
				return getValue.transform(value -> arguments -> instantiate.apply(value, arguments));
			}

			@Override
			public Optional<PropertyValue<T>> getDefault(String keyString) {
				if (defaultValue != null) {
					return Optional.of(arguments -> instantiate.apply(defaultValue.apply(keyString), arguments));
				} else {
					return empty();
				}
			}
		};
	}

	static <C, T> PropertyValueProvider<T> over(Parser<C> getValue, BiFunction<C, List<?>, T> instantiate) {
		return over(getValue, instantiate, k -> null);
	}

	static <T> PropertyValueProvider<T> over(Parser<T> getValue) {
		return over(getValue, (c, p) -> c);
	}
}
