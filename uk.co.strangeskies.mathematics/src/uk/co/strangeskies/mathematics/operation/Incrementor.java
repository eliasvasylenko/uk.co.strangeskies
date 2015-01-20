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

public interface Incrementor<T> {
	public T increment(T value);

	public T decrement(T value);

	public T getIncremented(T value);

	public T getDecremented(T value);

	public default T increment(T value, int amount) {
		for (int i = 0; i < amount; i++)
			increment(value);
		for (int i = 0; i > amount; i--)
			decrement(value);

		return value;
	}

	public default T decrement(T value, int amount) {
		for (int i = 0; i < amount; i++)
			decrement(value);
		for (int i = 0; i > amount; i--)
			increment(value);

		return value;
	}

	public default T getIncremented(T value, int amount) {
		if (amount < 0)
			value = getDecremented(value, -amount);
		else {
			if (amount > 0)
				value = getIncremented(value);
			for (int i = 1; i < amount; i++)
				increment(value);
		}

		return value;
	}

	public default T getDecremented(T value, int amount) {
		if (amount < 0)
			value = getIncremented(value, -amount);
		else {
			if (amount > 0)
				value = getDecremented(value);
			for (int i = 1; i < amount; i++)
				decrement(value);
		}

		return value;
	}

	public default Incrementor<T> reversed() {
		Incrementor<T> incrementor = this;

		return new Incrementor<T>() {
			@Override
			public T increment(T value) {
				return incrementor.decrement(value);
			}

			@Override
			public T decrement(T value) {
				return incrementor.increment(value);
			}

			@Override
			public T getIncremented(T value) {
				return incrementor.getDecremented(value);
			}

			@Override
			public T getDecremented(T value) {
				return incrementor.getIncremented(value);
			}

			@Override
			public T increment(T value, int amount) {
				return incrementor.decrement(value, amount);
			}

			@Override
			public T decrement(T value, int amount) {
				return incrementor.increment(value, amount);
			}

			@Override
			public T getIncremented(T value, int amount) {
				return incrementor.getDecremented(value, amount);
			}

			@Override
			public T getDecremented(T value, int amount) {
				return incrementor.getIncremented(value, amount);
			}
		};
	}
}
