/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface represents a gettable and settable property of a given type.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the property.
 * @param <R>
 *          The supertype of the property type with which we can set a property.
 *          Commonly this is the same as {@code T}.
 */
/* @I */
public interface Property<T extends R, R> {
	/**
	 * Set the value of this property to the given value.
	 * 
	 * @param to
	 *          The new value to set for this property.
	 * @return The previous value of this property.
	 */
	T set(/* @Mutable Property<T, R> this, */R to);

	/**
	 * Get the current value of the property.
	 * 
	 * @return The current value.
	 */
	/* @I */T get();

	static <T extends R, R> Property<T, R> over(Supplier<T> get, Consumer<R> set) {
		return over(get, r -> {
			T previous = get.get();
			set.accept(r);
			return previous;
		});
	}

	static <T extends R, R> Property<T, R> over(Supplier<T> get, Function<R, T> set) {
		return new Property<T, R>() {
			@Override
			public T set(R to) {
				return set.apply(to);
			}

			@Override
			public T get() {
				return get.get();
			}

			@Override
			public String toString() {
				return Objects.toString(get());
			}
		};
	}
}
