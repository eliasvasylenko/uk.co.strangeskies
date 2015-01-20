/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.values.Value;

public interface Matrix3<V extends Value<V>> extends MatrixS<Matrix3<V>, V> {
	@Override
	public List<Vector3<V>> getRowVectors();

	@Override
	public List<Vector3<V>> getColumnVectors();

	@Override
	public Vector3<V> getRowVector(int row);

	@Override
	public Vector3<V> getColumnVector(int column);

	@Override
	public List<Vector3<V>> getMajorVectors();

	@Override
	public List<Vector3<V>> getMinorVectors();

	@Override
	public Vector3<V> getMajorVector(int index);

	@Override
	public Vector3<V> getMinorVector(int index);
}