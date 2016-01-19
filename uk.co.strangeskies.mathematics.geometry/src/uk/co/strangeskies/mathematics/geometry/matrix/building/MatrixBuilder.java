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
package uk.co.strangeskies.mathematics.geometry.matrix.building;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.FloatValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.LongValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public interface MatrixBuilder {
	public Order getDefaultOrder();

	public void setDefaultOrder(Order defaultOrder);

	public Orientation getDefaultOrientation();

	public void setDefaultOrientation(Orientation defaultOrientation);

	public ValueMatrixBuilder<IntValue> ints();

	public ValueMatrixBuilder<LongValue> longs();

	public ValueMatrixBuilder<FloatValue> floats();

	public ValueMatrixBuilder<DoubleValue> doubles();

	public <V extends Value<V>> ValueMatrixBuilder<V> values(
			Factory<V> valueFactory);
}
