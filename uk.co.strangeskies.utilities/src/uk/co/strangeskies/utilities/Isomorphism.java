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
package uk.co.strangeskies.utilities;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class Isomorphism {
	private final Map<?, ?> copiedNodes = new IdentityHashMap<>();

	public <S> S getCopy(Copyable<? extends S> node) {
		return getCopy(node, Copyable::copy);
	}

	@SuppressWarnings("unchecked")
	public <S, C> S getCopy(C node, Function<C, S> copyFunction) {
		S copy = ((Map<C, S>) copiedNodes).computeIfAbsent(node, copyFunction::apply);
		((Map<S, S>) copiedNodes).put(copy, copy);
		return copy;
	}
}
