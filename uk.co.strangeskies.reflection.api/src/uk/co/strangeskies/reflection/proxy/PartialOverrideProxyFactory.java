/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.api.
 *
 * uk.co.strangeskies.reflection.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.proxy;

import java.util.function.Function;

import uk.co.strangeskies.reflection.TypeLiteral;

public interface PartialOverrideProxyFactory {
	public <T> Function<T, T> create(Class<T> baseClass,
			Class<? extends T> partialImplementation);

	public <T> Function<T, T> create(Class<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory);

	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Class<? extends T> partialImplementation);

	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory);
}
