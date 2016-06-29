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
import static java.util.Optional.ofNullable;

import java.util.List;
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
public interface PropertyProvider<T> {
	Class<T> getPropertyClass();

	Parser<PropertyValue<T>> getParser();

	default PropertyValue<T> getDefault(String keyString) {
		return p -> empty();
	}

	static <C, T> PropertyProvider<T> over(Class<T> propertyClass, Parser<C> getValue,
			BiFunction<C, List<?>, T> instantiate, Function<String, C> defaultValue) {
		return new PropertyProvider<T>() {
			@Override
			public Class<T> getPropertyClass() {
				return propertyClass;
			}

			@Override
			public Parser<PropertyValue<T>> getParser() {
				return getValue.transform(c -> p -> ofNullable(instantiate.apply(c, p)));
			}

			@Override
			public PropertyValue<T> getDefault(String keyString) {
				return p -> ofNullable(defaultValue.apply(keyString)).map(d -> instantiate.apply(d, p));
			}
		};
	}

	static <C, T> PropertyProvider<T> over(Class<T> propertyClass, Parser<C> getValue,
			BiFunction<C, List<?>, T> instantiate) {
		return over(propertyClass, getValue, instantiate, k -> null);
	}

	static <T> PropertyProvider<T> over(Class<T> propertyClass, Parser<T> getValue) {
		return over(propertyClass, getValue, (c, p) -> c);
	}
}
