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
package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix3;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector4Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorH3Impl;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class MatrixH3Impl<V extends Value<V>> extends
		MatrixHImpl<MatrixH3<V>, V> implements MatrixH3<V> {
	public MatrixH3Impl(Order order, Factory<V> valueFactory) {
		super(3, order, valueFactory);
	}

	public MatrixH3Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		Matrix.assertDimensions(getThis(), 4);
	}

	@Override
	public Matrix3<V> getTransformationMatrix() {
		return new Matrix3Impl<V>(getOrder(), getTransformationData2());
	}

	@Override
	public MatrixH3Impl<V> copy() {
		return new MatrixH3Impl<V>(getOrder(), getData2());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector4<V>> getRowVectors() {
		return (List<Vector4<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorH3<V>> getColumnVectors() {
		return (List<VectorH3<V>>) super.getColumnVectors();
	}

	@Override
	public final Vector4<V> getRowVector(int row) {
		return new Vector4Impl<V>(getOrder(), Orientation.ROW,
				getRowVectorData(row));
	}

	@Override
	public final VectorH3<V> getColumnVector(int column) {
		return new VectorH3Impl<V>(column == getDimensions() - 1 ? Type.Absolute
				: Type.Relative, getOrder(), Orientation.COLUMN, getColumnVectorData(
				getProjectedDimensions()).subList(0, getProjectedDimensions()));
	}

	@Override
	public final Vector<?, V> getMajorVector(int index) {
		List<V> majorElements = getData2().get(index);

		if (getOrder() == Order.COLUMN_MAJOR) {
			majorElements = majorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorH3Impl<V>(newType, Order.COLUMN_MAJOR,
					Orientation.COLUMN, majorElements);
		} else {
			return new Vector4Impl<V>(Order.ROW_MAJOR, Orientation.ROW, getData2()
					.get(index));
		}
	}

	@Override
	public final Vector<?, V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}

		if (getOrder() == Order.ROW_MAJOR) {
			minorElements = minorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorH3Impl<V>(newType, Order.ROW_MAJOR, Orientation.COLUMN,
					minorElements);
		} else {
			return new Vector4Impl<V>(Order.COLUMN_MAJOR, Orientation.ROW, getData2()
					.get(index));
		}
	}
}
