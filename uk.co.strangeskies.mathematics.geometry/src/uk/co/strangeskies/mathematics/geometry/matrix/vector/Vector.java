/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.geometry.Translatable;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

public interface Vector<S extends Vector<S, V>, V extends Value<V>>
	extends Self<S>, Matrix<S, V>, Translatable<S> {
	public enum Orientation {
	ROW {
		@Override
		public Order getAssociatedOrder() {
		return Order.ROW_MAJOR;
		}

		@Override
		public Orientation getOther() {
		return COLUMN;
		}
	},
	COLUMN {
		@Override
		public Order getAssociatedOrder() {
		return Order.COLUMN_MAJOR;
		}

		@Override
		public Orientation getOther() {
		return ROW;
		}
	};

	public abstract Order getAssociatedOrder();

	public abstract Orientation getOther();
	}

	@Override
	public default S transpose() {
	Matrix.assertIsSquare(this);
	return null;
	}

	public int getDimensions();

	public Orientation getOrientation();

	public V getElement(int index);

	public DoubleValue getSize();

	public V getSizeSquared();

	public S setData(boolean setByReference, Vector<?, V> to);

	public S setData(Vector<?, ?> to);
}
