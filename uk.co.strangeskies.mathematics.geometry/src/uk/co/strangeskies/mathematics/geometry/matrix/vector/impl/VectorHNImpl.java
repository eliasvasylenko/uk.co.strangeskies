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
package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorHN;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class VectorHNImpl<V extends Value<V>> extends
		VectorHImpl<VectorHN<V>, V> implements VectorHN<V> {
	public VectorHNImpl(Type type, int size, Order order,
			Orientation orientation, Factory<V> valueFactory) {
		super(type, size, order, orientation, valueFactory);
	}

	public VectorHNImpl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(type, order, orientation, values);
	}

	@Override
	public VectorNImpl<V> getMutableVector() {
		VectorNImpl<V> mutableVector = new VectorNImpl<>(getOrder(),
				getOrientation(), getData().subList(0, getProjectedDimensions()));

		return mutableVector;
	}

	@Override
	public VectorHN<V> copy() {
		return new VectorHNImpl<>(getType(), getOrder(), getOrientation(),
				getData());
	}
}
