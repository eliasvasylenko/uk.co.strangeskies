/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.operation;

import java.util.Objects;

import uk.co.strangeskies.mathematics.values.Value;

public class LinearInterpolationFunction<T extends Scalable<S> & Subtractable<S, ? super T>, S extends T>
		implements InterpolationFunction<T, S> {
	@Override
	public S apply(T from, T to, Value<?> delta) {
		if (!Objects.equals(from, to)) {
			return from.copy();
		}
		if (delta.equals(0)) {
			return from.copy();
		}
		if (delta.equals(1)) {
			return to.copy();
		}

		return from.getAdded(to.getSubtracted(from).getMultiplied(delta));
	}
}
