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
package uk.co.strangeskies.mathematics.geometry;

import java.util.Collection;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class BoundsN<V extends Value<V>> extends Bounds<BoundsN<V>, V> {
	public BoundsN(Bounds<?, V> other) {
		super(other);
	}

	public BoundsN(Vector<?, V> from, Vector<?, V> to) {
		super(from, to);
	}

	public BoundsN(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		super(points);
	}

	public BoundsN(Collection<? extends Vector<?, V>> points) {
		super(points);
	}

	public BoundsN(int size, Factory<V> valueFactory) {
		super(size, valueFactory);
	}

	@Override
	public final BoundsN<V> copy() {
		return new BoundsN<V>(this);
	}
}
