/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.utilities.classpath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PropertyType<T> {
	private static final List<PropertyType<?>> DEFAULT_TYPES = new ArrayList<>();

	public static final PropertyType<String> DIRECTIVE = reg(
			new PropertyType<>("", String.class, Function.identity(), Function.identity()));

	public static final PropertyType<String> STRING = reg(new PropertyType<>(String.class, Function.identity()));

	public static final PropertyType<String[]> STRINGS = reg(STRING.asListType());

	public static final PropertyType<Long> LONG = reg(new PropertyType<>(Long.class, Long::parseLong));

	public static final PropertyType<Long[]> LONGS = reg(LONG.asListType());

	public static final PropertyType<Double> DOUBLE = reg(new PropertyType<>(Double.class, Double::parseDouble));

	public static final PropertyType<Double[]> DOUBLES = reg(DOUBLE.asListType());

	private final String name;
	private final Class<T> type;

	private final Function<? super String, ? extends T> parser;
	private final Function<? super T, ? extends String> composer;

	public PropertyType(Class<T> type, Function<? super String, ? extends T> parser) {
		this(type.getSimpleName(), type, parser, Object::toString);
	}

	private static <T> PropertyType<T> reg(PropertyType<T> propertyType) {
		DEFAULT_TYPES.add(propertyType);
		return propertyType;
	}

	public PropertyType(String name, Class<T> type, Function<? super String, ? extends T> parser,
			Function<? super T, ? extends String> composer) {
		this.name = name;
		this.type = type;

		this.parser = parser;
		this.composer = composer;
	}

	public PropertyType<T[]> asListType() {
		@SuppressWarnings("unchecked")
		Class<T[]> arrayType = (Class<T[]>) Array.newInstance(type(), 0).getClass();

		@SuppressWarnings("unchecked")
		Function<String, T[]> parseToArray = s -> {
			String[] split = s.split("(?<!\\\\),", Integer.MIN_VALUE);

			List<T> items = new ArrayList<>();
			for (String substring : split) {
				substring = substring.replaceAll("\\\\,", ",").trim();

				items.add(parser.apply(substring));
			}

			return items.toArray((T[]) Array.newInstance(type, split.length));
		};

		Function<T[], String> composeFromArray = a -> {
			return Arrays.stream(a).map(composer::apply).map(s -> s.replaceAll(",", "\\,")).collect(Collectors.joining(","));
		};

		return new PropertyType<>("List<" + name() + ">", arrayType, parseToArray, composeFromArray);
	}

	public String name() {
		return name;
	}

	public Class<T> type() {
		return type;
	}

	public String composeString(T value) {
		return composer.apply(value);
	}

	public T parseString(String string) {
		return parser.apply(string);
	}

	public static PropertyType<?> fromName(String name) {
		return fromName(name, Collections.emptySet());
	}

	public static PropertyType<?> fromName(String name, PropertyType<?>... knownTypes) {
		return fromName(name, Arrays.asList(knownTypes));
	}

	public static PropertyType<?> fromName(String name, Collection<? extends PropertyType<?>> knownTypes) {
		Set<PropertyType<?>> allKnownTypes = new HashSet<>(knownTypes);
		allKnownTypes.addAll(DEFAULT_TYPES);

		return allKnownTypes.stream().filter(p -> p.name().equals(name)).findAny()
				.orElseThrow(() -> new RuntimeException("Cannot find property type " + name));
	}
}
