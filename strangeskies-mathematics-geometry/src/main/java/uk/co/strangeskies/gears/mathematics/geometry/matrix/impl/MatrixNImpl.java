package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixN;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class MatrixNImpl<V extends Value<V>> extends MatrixSImpl<MatrixN<V>, V>
		implements MatrixN<V> {
	public MatrixNImpl(int size, Order order, Factory<V> valueFactory) {
		super(size, order, valueFactory);
	}

	public MatrixNImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);
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

	@Override
	public MatrixN<V> copy() {
		return new MatrixNImpl<>(getOrder(), getData2());
	}
}
