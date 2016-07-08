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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.text.parsing.Parser;

/**
 * A provider of property values for {@link Properties properties} of a certain
 * class.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the property value
 */
public interface PropertyValueProvider<T> {
	Parser<T> getParser(List<?> arguments);

	default boolean providesDefault() {
		return false;
	}

	default T getDefault(String keyString, List<?> arguments) {
		throw new UnsupportedOperationException();
	}

	static <T> PropertyValueProvider<T> over(Function<List<?>, Parser<T>> getValue,
			BiFunction<String, List<?>, T> defaultValue) {
		return new PropertyValueProvider<T>() {
			@Override
			public Parser<T> getParser(List<?> arguments) {
				return getValue.apply(arguments);
			}

			@Override
			public boolean providesDefault() {
				return defaultValue != null;
			}

			@Override
			public T getDefault(String keyString, List<?> arguments) {
				if (defaultValue != null) {
					return defaultValue.apply(keyString, arguments);
				} else {
					return PropertyValueProvider.super.getDefault(keyString, arguments);
				}
			}
		};
	}

	static <T> PropertyValueProvider<T> over(Function<List<?>, Parser<T>> getValue) {
		return over(getValue, null);
	}

	static <T> PropertyValueProvider<T> over(Parser<T> getValue) {
		return over(arguments -> getValue);
	}
}
