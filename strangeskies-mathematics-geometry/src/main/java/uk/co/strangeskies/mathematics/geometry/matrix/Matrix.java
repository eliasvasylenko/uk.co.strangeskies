package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Addable;
import uk.co.strangeskies.mathematics.Negatable;
import uk.co.strangeskies.mathematics.NonCommutativelyMultipliable;
import uk.co.strangeskies.mathematics.Scalable;
import uk.co.strangeskies.mathematics.Subtractable;
import uk.co.strangeskies.mathematics.expression.Variable;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixNImpl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Copyable;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.function.TriFunction;
import uk.co.strangeskies.utilities.function.collection.ListTransformationView;

public interface Matrix<S extends Matrix<S, V>, V extends Value<V>> extends
		Self<S>, Copyable<S>, Comparable<Matrix<?, ?>>, Variable<S>,
		Addable<S, Matrix<?, ?>>, Scalable<S>, Subtractable<S, Matrix<?, ?>>,
		Negatable<S, S>, NonCommutativelyMultipliable<S, Matrix<?, ?>>,
		Property<S, Matrix<?, ?>> {
	public static enum Order {
		RowMajor {
			@Override
			public Orientation getAssociatedOrientation() {
				return Orientation.Row;
			}

			@Override
			public Order getOther() {
				return ColumnMajor;
			}
		},

		ColumnMajor {
			@Override
			public Orientation getAssociatedOrientation() {
				return Orientation.Column;
			}

			@Override
			public Order getOther() {
				return RowMajor;
			}
		};

		public abstract Orientation getAssociatedOrientation();

		public abstract Order getOther();
	}

	@Override
	public default S set(Matrix<?, ?> to) {
		return setData2(to.withOrder(getOrder()).getData2());
	}

	@Override
	public default S get() {
		return getThis();
	}

	public Order getOrder();

	/**
	 * Creates a view of this matrix with the requested order. Changes to the view
	 * affect this matrix and vice versa.
	 *
	 * @return
	 */
	public default Matrix<?, V> withOrder(Order order) {
		if (order == getOrder())
			return this;
		else
			return new ReOrderedMatrix<V>(this);
	}

	public Vector<?, V> getMajorVector(int index);

	public Vector<?, V> getMinorVector(int index);

	public List<V> getData();

	public List<List<V>> getData2();

	/**
	 * Get the dimensions as: [columns, rows].
	 *
	 * @return A vector representing the current dimensions.
	 */
	public default Vector2<IntValue> getDimensions2() {
		return new Vector2Impl<IntValue>(Order.ColumnMajor, Orientation.Column,
				IntValue.factory()).setData(getRowSize(), getColumnSize());
	}

	@Override
	public default S negate() {
		return operateOnData(element -> element.negate());
	}

	@Override
	public default S multiply(Value<?> scalar) {
		return operateOnData(element -> element.multiply(scalar));
	}

	@Override
	public default S multiply(long scalar) {
		return operateOnData(element -> element.multiply(scalar));
	}

	@Override
	public default S multiply(double scalar) {
		return operateOnData(element -> element.multiply(scalar));
	}

	@Override
	public default S divide(Value<?> scalar) {
		return operateOnData(element -> element.divide(scalar));
	}

	@Override
	public default S divide(long scalar) {
		return operateOnData(element -> element.divide(scalar));
	}

	@Override
	public default S divide(double scalar) {
		return operateOnData(element -> element.divide(scalar));
	}

	@Override
	public default S add(Matrix<?, ?> other) {
		return operateOnData2(other.withOrder(getOrder()).getData2(), (
				firstOperand, secondOperand) -> firstOperand.add(secondOperand));
	}

	@Override
	public default S subtract(Matrix<?, ?> other) {
		return operateOnData2(other.withOrder(getOrder()).getData2(), (
				firstOperand, secondOperand) -> firstOperand.subtract(secondOperand));
	}

	@Override
	public default S multiply(Matrix<?, ?> other) {
		return setData2(multiplyData(getData2(), other.withOrder(getOrder())
				.getData2()));
	}

	@Override
	public default S preMultiply(Matrix<?, ?> other) {
		return setData2(preMultiplyData(getData2(), other.withOrder(getOrder())
				.getData2()));
	}

	public static <V extends Value<?>> List<List<V>> multiplyData(
			List<? extends List<? extends Value<?>>> data,
			List<? extends List<? extends Value<?>>> otherData) {
		// TODO implement multiplication! include isResiseable() in parameter
		// dimensions check...
		return null;
	}

	public static <V extends Value<?>> List<List<V>> preMultiplyData(
			List<? extends List<? extends Value<?>>> data,
			List<? extends List<? extends Value<?>>> otherData) {
		return null;
	}

	public S transpose();

	public default Matrix<?, V> getTransposed() {
		return new MatrixNImpl<V>(getOrder(), transposeData(getData2()));
	}

	public static <V extends Value<V>> List<List<V>> transposeData(
			List<List<V>> data) {
		int majorSize = data.get(0).size();
		int minorSize = data.size();

		List<List<V>> transposedData = new ArrayList<List<V>>();
		for (int i = 0; i < majorSize; i++) {
			List<V> elements = new ArrayList<V>();
			transposedData.add(elements);
			for (int j = 0; j < minorSize; j++) {
				elements.add(data.get(j).get(i));
			}
		}
		return transposedData;
	}

	public default int getDataSize() {
		return getMajorSize() * getMinorSize();
	}

	public int getMajorSize();

	public int getMinorSize();

	public V getElement(int major, int minor);

	public default int getRowSize() {
		return getOrder() == Order.RowMajor ? getMajorSize() : getMinorSize();
	}

	public default int getColumnSize() {
		return getOrder() == Order.ColumnMajor ? getMajorSize() : getMinorSize();
	}

	public default boolean isSquare() {
		return getMajorSize() == getMinorSize();
	}

	public static <T extends Matrix<?, ?>> T assertIsSquare(T matrix) {
		try {
			DimensionalityException.checkEquivalence(matrix.getMajorSize(),
					matrix.getMinorSize());
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		return matrix;
	}

	public static <T extends Matrix<?, ?>> T assertDimensions(T matrix, int rows,
			int columns) {
		try {
			DimensionalityException.checkEquivalence(matrix.getRowSize(), rows);
			DimensionalityException.checkEquivalence(matrix.getColumnSize(), columns);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		return matrix;
	}

	public static <T extends Matrix<?, ?>> T assertDimensions(T matrix, int size) {
		return assertDimensions(matrix, size, size);
	}

	public default List<? extends Vector<?, V>> getRowVectors() {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	public default List<? extends Vector<?, V>> getColumnVectors() {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	public default Vector<?, V> getRowVector(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVector(row);
		} else {
			return getMinorVector(row);
		}
	}

	public default Vector<?, V> getColumnVector(int column) {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVector(column);
		} else {
			return getMinorVector(column);
		}
	}

	public default List<? extends Vector<?, V>> getMajorVectors() {
		return new AbstractList<Vector<?, V>>() {
			@Override
			public Vector<?, V> get(int index) {
				return getMajorVector(index);
			}

			@Override
			public int size() {
				return getMinorSize();
			}
		};
	}

	public default List<? extends Vector<?, V>> getMinorVectors() {
		return new AbstractList<Vector<?, V>>() {
			@Override
			public Vector<?, V> get(int index) {
				return getMinorVector(index);
			}

			@Override
			public int size() {
				return getMajorSize();
			}
		};
	}

	public default int[] getData(int[] dataArray) {
		operateOnData((value, index) -> {
			dataArray[index] = value.intValue();
			return value;
		});
		return dataArray;
	}

	public default long[] getData(long[] dataArray) {
		operateOnData((value, index) -> {
			dataArray[index] = value.longValue();
			return value;
		});
		return dataArray;
	}

	public default float[] getData(float[] dataArray) {
		operateOnData((value, index) -> {
			dataArray[index] = value.floatValue();
			return value;
		});
		return dataArray;
	}

	public default double[] getData(double[] dataArray) {
		operateOnData((value, index) -> {
			dataArray[index] = value.doubleValue();
			return value;
		});
		return dataArray;
	}

	public default int[][] getData2(int[][] dataArray) {
		operateOnData2((value, i, j) -> {
			dataArray[i][j] = value.intValue();
			return value;
		});
		return dataArray;
	}

	public default long[][] getData2(long[][] dataArray) {
		operateOnData2((value, i, j) -> {
			dataArray[i][j] = value.longValue();
			return value;
		});
		return dataArray;
	}

	public default float[][] getData2(float[][] dataArray) {
		operateOnData2((value, i, j) -> {
			dataArray[i][j] = value.floatValue();
			return value;
		});
		return dataArray;
	}

	public default double[][] getData2(double[][] dataArray) {
		operateOnData2((value, i, j) -> {
			dataArray[i][j] = value.doubleValue();
			return value;
		});
		return dataArray;
	}

	public default int[] getIntData() {
		return getData(new int[getDataSize()]);
	}

	public default long[] getLongData() {
		return getData(new long[getDataSize()]);
	}

	public default float[] getFloatData() {
		return getData(new float[getDataSize()]);
	}

	public default double[] getDoubleData() {
		return getData(new double[getDataSize()]);
	}

	public default int[][] getIntData2() {
		return getData2(new int[getMajorSize()][getMinorSize()]);
	}

	public default long[][] getLongData2() {
		return getData2(new long[getMajorSize()][getMinorSize()]);
	}

	public default float[][] getFloatData2() {
		return getData2(new float[getMajorSize()][getMinorSize()]);
	}

	public default double[][] getDoubleData2() {
		return getData2(new double[getMajorSize()][getMinorSize()]);
	}

	public S operateOnData(Function<? super V, ? extends V> operator);

	public S operateOnData(BiFunction<? super V, Integer, ? extends V> operator);

	public S operateOnData2(
			TriFunction<? super V, Integer, Integer, ? extends V> operator);

	public default <I> S operateOnData(List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData((value, index) -> operator.apply(value,
				itemList.get(index)));
	}

	public default <I> S operateOnData2(
			List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData2((value, i, j) -> operator.apply(value, itemList
				.get(i).get(j)));
	}

	public default S setData2(boolean setByReference,
			List<? extends List<? extends V>> to) {
		if (setByReference)
			return operateOnData2(to,
					(V firstOperand, V secondOperand) -> secondOperand);
		else
			return operateOnData2(to,
					(V firstOperand, V secondOperand) -> firstOperand
							.setValue(secondOperand));
	}

	public default S setData(boolean setByReference, List<? extends V> to) {
		if (setByReference)
			return operateOnData(to,
					(V firstOperand, V secondOperand) -> secondOperand);
		else
			return operateOnData(to,
					(V firstOperand, V secondOperand) -> firstOperand
							.setValue(secondOperand));
	}

	@SuppressWarnings("unchecked")
	public default S setData(boolean setByReference, V... to) {
		return setData(setByReference, Arrays.asList(to));
	}

	public default S setData(Number... values) {
		return operateOnData(Arrays.asList(values),
				(assignee, assignment) -> assignee.setValue(assignment));
	}

	public default S setData(Value<?>... to) {
		return setData(Arrays.asList(to));
	}

	public default S setData(int[] to) {
		return operateOnData((value, index) -> value.setValue(to[index]));
	}

	public default S setData(long[] to) {
		return operateOnData((value, index) -> value.setValue(to[index]));
	}

	public default S setData(float[] to) {
		return operateOnData((value, index) -> value.setValue(to[index]));
	}

	public default S setData(double[] to) {
		return operateOnData((value, index) -> value.setValue(to[index]));
	}

	public default S setData2(int[]... to) {
		return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
	}

	public default S setData2(long[]... to) {
		return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
	}

	public default S setData2(float[]... to) {
		return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
	}

	public default S setData2(double[]... to) {
		return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
	}

	@SuppressWarnings("unchecked")
	public default S setData2(boolean copyByReference, Vector<?, V>... to) {
		return setData2(copyByReference,
				new ListTransformationView<>(Arrays.asList(to), i -> i.getData()));
	}

	public default S setData2(Vector<?, ?>... values) {
		return setData2(new ListTransformationView<>(Arrays.asList(values),
				i -> i.getData()));
	}

	public default S setData2(List<? extends List<? extends Value<?>>> to) {
		return operateOnData2(to,
				(assignee, assignment) -> assignee.set(assignment));
	}

	public default S setData(List<? extends Value<?>> to) {
		return operateOnData(to, (assignee, assignment) -> assignee.set(assignment));
	}
}
