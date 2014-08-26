package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix4;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector4Impl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Matrix4Impl<V extends Value<V>> extends MatrixSImpl<Matrix4<V>, V>
		implements Matrix4<V> {
	public Matrix4Impl(Order order, Factory<V> valueFactory) {
		super(4, order, valueFactory);
	}

	public Matrix4Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		assertDimensions(this, 4);
	}

	@Override
	public Matrix4<V> copy() {
		return new Matrix4Impl<V>(getOrder(), getData2());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector4<V>> getRowVectors() {
		return (List<Vector4<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector4<V>> getColumnVectors() {
		return (List<Vector4<V>>) super.getColumnVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector4<V> getRowVector(int row) {
		return (Vector4<V>) super.getRowVector(row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector4<V> getColumnVector(int column) {
		return (Vector4<V>) super.getColumnVector(column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector4<V>> getMajorVectors() {
		return (List<Vector4<V>>) super.getMajorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector4<V>> getMinorVectors() {
		return (List<Vector4<V>>) super.getMinorVectors();
	}

	@Override
	public final Vector4<V> getMajorVector(int index) {
		return new Vector4Impl<V>(getOrder(),
				getOrder().getAssociatedOrientation(), getData2().get(index));
	}

	@Override
	public final Vector4<V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}
		return new Vector4Impl<V>(getOrder(), getOrder().getAssociatedOrientation()
				.getOther(), minorElements);
	}
}
