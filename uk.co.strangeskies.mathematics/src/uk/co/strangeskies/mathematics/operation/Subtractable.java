/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import uk.co.strangeskies.utilities.Self;

public interface Subtractable<S extends Subtractable<S, T>, T> extends
		Addable<S, T>, Self<S> {
	/**
	 * subtract the value from this
	 *
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public S subtract(Subtractable<S, T> this, T value);

	/**
	 * subtract the value from a copy of this
	 *
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public default S getSubtracted(Subtractable<S, T> this, T value) {
		return copy().subtract(value);
	}
}
