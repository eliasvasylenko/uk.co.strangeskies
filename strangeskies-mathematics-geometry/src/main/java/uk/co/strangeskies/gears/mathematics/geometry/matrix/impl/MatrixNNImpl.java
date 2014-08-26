package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixNN;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class MatrixNNImpl<V extends Value<V>> extends
		MatrixImpl<MatrixNN<V>, V> implements MatrixNN<V> {
	public MatrixNNImpl(int rows, int columns, Order order,
			Factory<V> valueFactory) {
		super(rows, columns, order, valueFactory);
	}

	public MatrixNNImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);
	}

	@Override
	public MatrixNN<V> getTransposed() {
		return ((MatrixNNImpl<V>) copy()).transposeImplementation();
	}

	@Override
	public boolean isResizable() {
		return false;
	}

	@Override
	public MatrixNN<V> copy() {
		return new MatrixNNImpl<V>(getOrder(), getData2());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorNImpl<V>> getRowVectors() {
		return (List<VectorNImpl<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorNImpl<V>> getColumnVectors() {
		return (List<VectorNImpl<V>>) super.getColumnVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final VectorNImpl<V> getRowVector(int row) {
		return (VectorNImpl<V>) super.getRowVector(row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final VectorNImpl<V> getColumnVector(int column) {
		return (VectorNImpl<V>) super.getColumnVector(column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorNImpl<V>> getMajorVectors() {
		return (List<VectorNImpl<V>>) super.getMajorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorNImpl<V>> getMinorVectors() {
		return (List<VectorNImpl<V>>) super.getMinorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final VectorNImpl<V> getMajorVector(int index) {
		return (VectorNImpl<V>) super.getMajorVector(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final VectorNImpl<V> getMinorVector(int index) {
		return (VectorNImpl<V>) super.getMinorVector(index);
	}
}
