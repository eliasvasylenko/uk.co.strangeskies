package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixRR;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class MatrixRRImpl<V extends Value<V>> extends
		MatrixImpl<MatrixRR<V>, V> implements MatrixRR<V> {
	public MatrixRRImpl(int rows, int columns, Order order,
			Factory<V> valueFactory) {
		super(rows, columns, order, valueFactory);
	}

	public MatrixRRImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);
	}

	@Override
	public MatrixRR<V> getTransposed() {
		return ((MatrixRRImpl<V>) copy()).transposeImplementation();
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public MatrixRR<V> copy() {
		return new MatrixRRImpl<V>(getOrder(), getData2());
	}

	@Override
	public MatrixRR<V> resize(Vector<?, IntValue> dimensions) {
		return super.resizeImplementation(dimensions);
	}

	@Override
	public MatrixRR<V> resize(int rows, int columns) {
		return super.resizeImplementation(rows, columns);
	}

	@Override
	public MatrixRR<V> resizeColumns(int dimensions) {
		return super.resizeColumnsImplementation(dimensions);
	}

	@Override
	public MatrixRR<V> resizeMajor(int dimensions) {
		return super.resizeMajorImplementation(dimensions);
	}

	@Override
	public MatrixRR<V> resizeMinor(int dimensions) {
		return super.resizeMinorImplementation(dimensions);
	}

	@Override
	public MatrixRR<V> resizeRows(int dimensions) {
		return super.resizeRowsImplementation(dimensions);
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
