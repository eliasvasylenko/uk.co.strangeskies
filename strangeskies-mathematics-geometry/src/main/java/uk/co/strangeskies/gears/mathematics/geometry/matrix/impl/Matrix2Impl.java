package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class Matrix2Impl<V extends Value<V>> extends MatrixSImpl<Matrix2<V>, V>
		implements Matrix2<V> {
	public Matrix2Impl(Order order, Factory<V> valueFactory) {
		super(2, order, valueFactory);
	}

	public Matrix2Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		assertDimensions(this, 2);
	}

	@Override
	public Matrix2<V> copy() {
		return new Matrix2Impl<V>(getOrder(), getData2());
	}

	@Override
	public V getDeterminant() {
		return getElement(0, 0).getMultiplied(getElement(1, 1)).add(
				getElement(0, 1).getMultiplied(getElement(1, 0)));
	}

	@Override
	public Matrix2<V> getRotated(Value<?> angle) {
		return copy().rotate(angle);
	}

	@Override
	public Matrix2<V> rotate(Value<?> angle) {
		// TODO implement rotation
		return null;
	}

	@Override
	public Matrix2<V> getPreRotated(Value<?> value) {
		return copy().preRotate(value);
	}

	@Override
	public Matrix2<V> preRotate(Value<?> value) {
		// TODO implement pre-rotation
		return null;
	}

	@Override
	public Matrix2<V> getPreRotated(Value<?> angle, Vector2<?> centre) {
		return copy().preRotate(angle, centre);
	}

	@Override
	public Matrix2<V> getRotated(Value<?> angle, Vector2<?> centre) {
		return copy().rotate(angle, centre);
	}

	@Override
	public Matrix2<V> rotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement rotation about point
		return null;
	}

	@Override
	public Matrix2<V> preRotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement pre-rotation about point
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getRowVectors() {
		return (List<Vector2<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getColumnVectors() {
		return (List<Vector2<V>>) super.getColumnVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector2Impl<V> getRowVector(int row) {
		return (Vector2Impl<V>) super.getRowVector(row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector2Impl<V> getColumnVector(int column) {
		return (Vector2Impl<V>) super.getColumnVector(column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getMajorVectors() {
		return (List<Vector2<V>>) super.getMajorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getMinorVectors() {
		return (List<Vector2<V>>) super.getMinorVectors();
	}

	@Override
	public final Vector2Impl<V> getMajorVector(int index) {
		return new Vector2Impl<V>(getOrder(),
				getOrder().getAssociatedOrientation(), getData2().get(index));
	}

	@Override
	public final Vector2Impl<V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}
		return new Vector2Impl<V>(getOrder(), getOrder().getOther()
				.getAssociatedOrientation(), minorElements);
	}
}
