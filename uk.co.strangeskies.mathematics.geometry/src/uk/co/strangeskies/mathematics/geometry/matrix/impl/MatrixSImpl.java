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
package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixS;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public abstract class MatrixSImpl<S extends MatrixS<S, V>, V extends Value<V>>
		extends MatrixImpl<S, V> implements MatrixS<S, V> {
	public MatrixSImpl(int size, Order order, Factory<V> valueFactory) {
		super(size, size, order, valueFactory);
	}

	public MatrixSImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		Matrix.assertIsSquare(this);
	}

	@Override
	public V getDeterminant() {
		return getDeterminant(this);
	}

	public static <V extends Value<V>> V getDeterminant(MatrixS<?, V> matrixSImpl) {
		// TODO implement...
		return null;
	}

	@Override
	public int getDimensions() {
		return getMinorSize();
	}

	@Override
	public boolean isSquare() {
		return true;
	}
}
