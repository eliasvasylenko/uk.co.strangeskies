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
package uk.co.strangeskies.utilities.function;

import java.util.function.Consumer;

/**
 * As {@link Consumer} but parameterized over an exception type which is allowed
 * to be thrown by {@link #accept(Object)}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the item to accept
 * @param <E>
 *          the type of exception which may be thrown
 */
public interface ThrowingConsumer<T, E extends Exception> {
	/**
	 * @param value
	 *          an instance of the expected type
	 * @throws E
	 *           an exception thrown by the implementor
	 */
	void accept(T value) throws E;
}
