package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import uk.co.strangeskies.mathematics.values.DoubleArrayListView;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.DoubleValueFactory;
import uk.co.strangeskies.mathematics.values.FloatArrayListView;
import uk.co.strangeskies.mathematics.values.FloatValue;
import uk.co.strangeskies.mathematics.values.FloatValueFactory;
import uk.co.strangeskies.mathematics.values.IntArrayListView;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.IntValueFactory;
import uk.co.strangeskies.mathematics.values.LongArrayListView;
import uk.co.strangeskies.mathematics.values.LongValue;
import uk.co.strangeskies.mathematics.values.LongValueFactory;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Copyable;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.function.AssignmentOperation;
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
		return setData2(to.getOrder(), to.getData2());
	}

	@Override
	public default S get() {
		return getThis();
	}

	public Order getOrder();

	public Vector<?, V> getMajorVector(int index);

	public Vector<?, V> getMinorVector(int index);

	public List<V> getData();

	public List<List<V>> getData2();

	public Vector2<IntValue> getDimensions2();

	@Override
	public default S negate() {
		operateOnData(element -> element.negate());
		return getThis();
	}

	@Override
	public default S multiply(Value<?> scalar) {
		operateOnData(element -> element.multiply(scalar));
		return getThis();
	}

	@Override
	public default S multiply(long scalar) {
		operateOnData(element -> element.multiply(scalar));
		return getThis();
	}

	@Override
	public default S multiply(double scalar) {
		operateOnData(element -> element.multiply(scalar));
		return getThis();
	}

	@Override
	public default S divide(Value<?> scalar) {
		operateOnData(element -> element.divide(scalar));
		return getThis();
	}

	@Override
	public default S divide(long scalar) {
		operateOnData(element -> element.divide(scalar));
		return getThis();
	}

	@Override
	public default S divide(double scalar) {
		operateOnData(element -> element.divide(scalar));
		return getThis();
	}

	@Override
	public default S add(Matrix<?, ?> other) {
		operateOnData2(other.getOrder(), other.getData2(), (firstOperand,
				secondOperand) -> firstOperand.add(secondOperand));

		return getThis();
	}

	@Override
	public default S subtract(Matrix<?, ?> other) {
		operateOnData2(other.getOrder(), other.getData2(), (firstOperand,
				secondOperand) -> firstOperand.subtract(secondOperand));

		return getThis();
	}

	@Override
	public default S multiply(Matrix<?, ?> other) {
		setData2(multiplyData(this, other.getData2()));

		return getThis();
	}

	@Override
	public default S preMultiply(Matrix<?, ?> other) {
		setData2(preMultiplyData(this, other.getData2()));

		return getThis();
	}

	public static <V extends Value<V>> List<List<Value<V>>> multiplyData(
			Matrix<?, V> matrix, List<? extends List<? extends Value<?>>> otherData) {
		// TODO implement multiplication! include isResiseable() in parameter
		// dimensions check...

		return null;
	}

	public static <V extends Value<V>> List<List<Value<V>>> preMultiplyData(
			Matrix<?, V> matrix, List<? extends List<? extends Value<?>>> otherData) {
		// TODO as above.

		return null;
	}

	public default Matrix<?, V> getTransposed() {
		return new MatrixNImpl<V>(getOrder(), getData2()).transpose();
	}

	public default int getDataSize() {
		return getMajorSize() * getMinorSize();
	}

	public int getMajorSize();

	public int getMinorSize();

	public V getElement(int major, int minor);

	public default int getMajorSize(Order order) {
		if (order == getOrder()) {
			return getMajorSize();
		} else {
			return getMinorSize();
		}
	}

	public default int getMinorSize(Order order) {
		if (order == getOrder()) {
			return getMinorSize();
		} else {
			return getMajorSize();
		}
	}

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
		try {
			DimensionalityException.checkEquivalence(matrix.getMajorSize(), size);
			DimensionalityException.checkEquivalence(matrix.getMinorSize(), size);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		return matrix;
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

	public int[] getData(int[] dataArray, Order order);

	public long[] getData(long[] dataArray, Order order);

	public float[] getData(float[] dataArray, Order order);

	public double[] getData(double[] dataArray, Order order);

	public int[][] getData2(int[][] dataArray, Order order);

	public long[][] getData2(long[][] dataArray, Order order);

	public float[][] getData2(float[][] dataArray, Order order);

	public double[][] getData2(double[][] dataArray, Order order);

	public default int[] getIntData(Order order) {
		return getData(new int[getDataSize()], order);
	}

	public default long[] getLongData(Order order) {
		return getData(new long[getDataSize()], order);
	}

	public default float[] getFloatData(Order order) {
		return getData(new float[getDataSize()], order);
	}

	public default double[] getDoubleData(Order order) {
		return getData(new double[getDataSize()], order);
	}

	public default int[] getData(int[] dataArray) {
		return getData(dataArray, getOrder());
	}

	public default long[] getData(long[] dataArray) {
		return getData(dataArray, getOrder());
	}

	public default float[] getData(float[] dataArray) {
		return getData(dataArray, getOrder());
	}

	public default double[] getData(double[] dataArray) {
		return getData(dataArray, getOrder());
	}

	public default int[] getIntData() {
		return getIntData(getOrder());
	}

	public default long[] getLongData() {
		return getLongData(getOrder());
	}

	public default float[] getFloatData() {
		return getFloatData(getOrder());
	}

	public default double[] getDoubleData() {
		return getDoubleData(getOrder());
	}

	public default int[][] getIntData2(Order order) {
		return getData2(new int[getMajorSize(order)][getMinorSize(order)], order);
	}

	public default long[][] getLongData2(Order order) {
		return getData2(new long[getMajorSize(order)][getMinorSize(order)], order);
	}

	public default float[][] getFloatData2(Order order) {
		return getData2(new float[getMajorSize(order)][getMinorSize(order)], order);
	}

	public default double[][] getDoubleData2(Order order) {
		return getData2(new double[getMajorSize(order)][getMinorSize(order)], order);
	}

	public default int[][] getData2(int[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	public default long[][] getData2(long[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	public default float[][] getData2(float[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	public default double[][] getData2(double[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	public default int[][] getIntData2() {
		return getIntData2(getOrder());
	}

	public default long[][] getLongData2() {
		return getLongData2(getOrder());
	}

	public default float[][] getFloatData2() {
		return getFloatData2(getOrder());
	}

	public default double[][] getDoubleData2() {
		return getDoubleData2(getOrder());
	}

	public S operateOnData(Function<? super V, ? extends V> operator);

	public <I> S operateOnData(Order order, List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public <I> S operateOnData2(Order order,
			List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public default <I> S operateOnData(List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData(getOrder(), itemList, operator);
	}

	public default <I> S operateOnData2(
			List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData2(getOrder(), itemList, operator);
	}

	public default <I> S setData(Order order, List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return operateOnData(order, itemList, operator);
	}

	public default <I> S setData(List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return setData(getOrder(), itemList, operator);
	}

	public default <I> S setData2(Order order,
			List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return operateOnData2(order, itemList, operator);
	}

	public default <I> S setData2(List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return setData2(getOrder(), itemList, operator);
	}

	public default S setData2(final boolean setByReference, Order order,
			List<? extends List<? extends V>> to) {
		if (setByReference) {
			return operateOnData2(order, to,
					(V firstOperand, V secondOperand) -> secondOperand);
		} else {
			return operateOnData2(order, to,
					(V firstOperand, V secondOperand) -> firstOperand
							.setValue(secondOperand));
		}
	}

	public default S setData2(boolean setByReference,
			List<? extends List<? extends V>> to) {
		return setData2(setByReference, getOrder(), to);
	}

	public default S setData(final boolean setByReference, Order order,
			List<? extends V> to) {
		if (setByReference) {
			return operateOnData(order, to,
					(V firstOperand, V secondOperand) -> secondOperand);
		} else {
			return operateOnData(order, to,
					(V firstOperand, V secondOperand) -> firstOperand
							.setValue(secondOperand));
		}
	}

	public default S setData(boolean setByReference, List<? extends V> to) {
		return setData(setByReference, getOrder(), to);
	}

	@SuppressWarnings("unchecked")
	public default S setData(boolean setByReference, Order order, V... to) {
		return setData(setByReference, order, Arrays.asList(to));
	}

	@SuppressWarnings("unchecked")
	public default S setData(boolean setByReference, V... to) {
		return setData(setByReference, Arrays.asList(to));
	}

	public default S setData(Order order, Number... values) {
		return setData(order, Arrays.asList(values),
				(V assignee, Number assignment) -> assignee.setValue(assignment));
	}

	public default S setData(Number... values) {
		return setData(getOrder(), values);
	}

	public default S setData(Order order, Value<?>... to) {
		return setData(order, Arrays.asList(to));
	}

	public default S setData(Value<?>... to) {
		return setData(Arrays.asList(to));
	}

	public default S setData(Order order, int[] to) {
		return setData(order,
				new IntArrayListView<>(to, IntValueFactory.instance()));
	}

	public default S setData(Order order, long[] to) {
		return setData(order,
				new LongArrayListView<>(to, LongValueFactory.instance()));
	}

	public default S setData(Order order, float[] to) {
		return setData(order,
				new FloatArrayListView<>(to, FloatValueFactory.instance()));
	}

	public default S setData(Order order, double[] to) {
		return setData(order,
				new DoubleArrayListView<>(to, DoubleValueFactory.instance()));
	}

	public default S setData(int[] to) {
		return setData(getOrder(), to);
	}

	public default S setData(long[] to) {
		return setData(getOrder(), to);
	}

	public default S setData(float[] to) {
		return setData(getOrder(), to);
	}

	public default S setData(double[] to) {
		return setData(getOrder(), to);
	}

	public default S setData2(Order order, int[]... data) {
		return setData2(Arrays
				.stream(data)
				.map(data2 -> new IntArrayListView<>(data2, IntValueFactory.instance()))
				.collect(Collectors.toList()));
	}

	public default S setData2(Order order, long[]... to) {
		return setData2(Arrays
				.stream(to)
				.map(
						d -> Arrays.stream(d).mapToObj(e -> new LongValue(e))
								.collect(Collectors.toList())).collect(Collectors.toList()));
	}

	public default S setData2(Order order, float[]... to) {
		return setData2(order,
				new ListTransformationView<float[], List<FloatValue>>(
						Arrays.asList(to), input -> new FloatArrayListView<>(input,
								FloatValueFactory.instance())));
	}

	public default S setData2(Order order, double[]... to) {
		return setData2(
				order,
				new ListTransformationView<double[], List<DoubleValue>>(Arrays
						.asList(to), input -> new DoubleArrayListView<>(input,
						DoubleValueFactory.instance())));
	}

	public default S setData2(int[]... to) {
		return setData2(getOrder(), to);
	}

	public default S setData2(long[]... to) {
		return setData2(getOrder(), to);
	}

	public default S setData2(float[]... to) {
		return setData2(getOrder(), to);
	}

	public default S setData2(double[]... to) {
		return setData2(getOrder(), to);
	}

	@SuppressWarnings("unchecked")
	public default S setData2(boolean copyByReference, Order order,
			Vector<?, V>... to) {
		return setData2(copyByReference, order,
				new ListTransformationView<>(Arrays.asList(to), i -> i.getData()));
	}

	@SuppressWarnings("unchecked")
	public default S setData2(boolean copyByReference, Vector<?, V>... values) {
		return setData2(getOrder(), values);
	}

	public default S setData2(Order order, Vector<?, ?>... values) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(values),
				i -> i.getData()));
	}

	public default S setData2(Vector<?, ?>... values) {
		return setData2(getOrder(), values);
	}

	public default S setData2(Order order,
			List<? extends List<? extends Value<?>>> to) {
		return setData2(order, to,
				(V assignee, Value<?> assignment) -> assignee.set(assignment));
	}

	public default S setData2(List<? extends List<? extends Value<?>>> to) {
		return setData2(getOrder(), to);
	}

	public default S setData(Order order, List<? extends Value<?>> to) {
		return setData(order, to,
				(V assignee, Value<?> assignment) -> assignee.set(assignment));
	}

	public default S setData(List<? extends Value<?>> to) {
		return setData(getOrder(), to);
	}
}