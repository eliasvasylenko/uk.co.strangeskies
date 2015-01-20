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
package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.NonCommutativelyTranslatable;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.values.Value;

public interface MatrixH<S extends MatrixH<S, V>, V extends Value<V>> extends
		NonCommutativelyTranslatable<S>, MatrixS<S, V> {
	public int getProjectedDimensions();

	public MatrixNN<V> getMutableMatrix();

	public MatrixS<?, V> getTransformationMatrix();

	@Override
	public List<? extends VectorH<?, V>> getColumnVectors();

	@Override
	public VectorH<?, V> getColumnVector(int column);
}