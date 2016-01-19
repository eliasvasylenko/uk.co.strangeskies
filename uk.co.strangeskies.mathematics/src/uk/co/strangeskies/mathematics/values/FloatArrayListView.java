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
package uk.co.strangeskies.mathematics.values;

import java.util.AbstractList;

import uk.co.strangeskies.utilities.factory.Factory;

public class FloatArrayListView<V extends Value<V>> extends AbstractList<V> {
	private final float[] array;
	private final Factory<V> valueFactory;

	public FloatArrayListView(float[] array, Factory<V> valueFactory) {
		if (array == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		this.array = array;
		this.valueFactory = valueFactory;
	}

	@Override
	public final V get(int index) {
		return valueFactory.create().setValue(array[index]);
	}

	public V set(int index, Value<?> element) {
		float previousValue = array[index];

		array[index] = element.intValue();

		return valueFactory.create().setValue(previousValue);
	}

	public Factory<V> getValueFactory() {
		return valueFactory;
	}

	@Override
	public final int size() {
		return array.length;
	}
}
