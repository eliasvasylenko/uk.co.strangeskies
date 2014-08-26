package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixHN;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorHNImpl;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class MatrixHNImpl<V extends Value<V>> extends
		MatrixHImpl<MatrixHN<V>, V> implements MatrixHN<V> {
	public MatrixHNImpl(int size, Order order, Factory<V> valueFactory) {
		super(size, order, valueFactory);
	}

	public MatrixHNImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);
	}

	@Override
	public MatrixNImpl<V> getTransformationMatrix() {
		return new MatrixNImpl<V>(getOrder(), getTransformationData2());
	}

	@Override
	public MatrixHN<V> copy() {
		return new MatrixHNImpl<>(getOrder(), getData2());
	}

	@Override
	public final Vector<?, V> getMajorVector(int index) {
		List<V> majorElements = getData2().get(index);

		if (getOrder() == Order.ColumnMajor) {
			majorElements = majorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorHNImpl<V>(newType, Order.ColumnMajor,
					Orientation.Column, majorElements);
		} else {
			return new VectorNImpl<V>(Order.RowMajor, Orientation.Row, getData2()
					.get(index));
		}
	}

	@Override
	public final Vector<?, V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}

		if (getOrder() == Order.RowMajor) {
			minorElements = minorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorHNImpl<V>(newType, Order.RowMajor, Orientation.Column,
					minorElements);
		} else {
			return new VectorNImpl<V>(Order.ColumnMajor, Orientation.Row, getData2()
					.get(index));
		}
	}
}
