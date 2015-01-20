/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.operation;

import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

public interface Scalable<S extends Scalable<S>> extends Self<S> {
	public S multiply(Value<?> value);

	public default S multiply(int value) {
		return multiply((long) value);
	}

	public S multiply(long value);

	public default S multiply(float value) {
		return multiply((double) value);
	}

	public S multiply(double value);

	public S divide(Value<?> value);

	public default S divide(int value) {
		return divide((long) value);
	}

	public S divide(long value);

	public default S divide(float value) {
		return divide((double) value);
	}

	public S divide(double value);

	public default S getMultiplied(Value<?> value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(int value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(long value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(float value) {
		return copy().multiply(value);
	}

	public default S getMultiplied(double value) {
		return copy().multiply(value);
	}

	public default S getDivided(Value<?> value) {
		return copy().divide(value);
	}

	public default S getDivided(int value) {
		return copy().divide(value);
	}

	public default S getDivided(long value) {
		return copy().divide(value);
	}

	public default S getDivided(float value) {
		return copy().divide(value);
	}

	public default S getDivided(double value) {
		return copy().divide(value);
	}
}
