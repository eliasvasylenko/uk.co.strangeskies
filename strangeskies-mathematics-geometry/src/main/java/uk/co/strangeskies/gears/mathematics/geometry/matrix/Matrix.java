package uk.co.strangeskies.gears.mathematics.geometry.matrix;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import checkers.oigj.quals.ReadOnly;
import uk.co.strangeskies.gears.mathematics.Addable;
import uk.co.strangeskies.gears.mathematics.Negatable;
import uk.co.strangeskies.gears.mathematics.NonCommutativelyMultipliable;
import uk.co.strangeskies.gears.mathematics.Scalable;
import uk.co.strangeskies.gears.mathematics.Subtractable;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.expression.Variable;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.Copyable;
import uk.co.strangeskies.gears.utilities.Property;
import uk.co.strangeskies.gears.utilities.Self;
import uk.co.strangeskies.gears.utilities.function.AssignmentOperation;

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

	public Order getOrder();

	public S setOrder(Order order);

	public List<? extends Vector<?, V>> getRowVectors();

	public List<? extends Vector<?, V>> getColumnVectors();

	public Vector<?, V> getRowVector(int row);

	public Vector<?, V> getColumnVector(int column);

	public List<? extends Vector<?, V>> getMajorVectors();

	public List<? extends Vector<?, V>> getMinorVectors();

	public Vector<?, V> getMajorVector(int index);

	public Vector<?, V> getMinorVector(int index);

	public List<V> getData();

	public List<List<V>> getData2();

	public Vector2<IntValue> getDimensions2();

	public Expression<Order> getOrderExpression();

	public Matrix<?, V> getTransposed();

	public int getDataSize();

	public int getMajorSize();

	public int getMinorSize();

	public int getMajorSize(Order order);

	public int getMinorSize(Order order);

	public int getRowSize();

	public int getColumnSize();

	public boolean isSquare();

	public boolean isResizable();

	public V getElement(int major, int minor);

	public int[] getData(int[] dataArray, Order order);

	public long[] getData(long[] dataArray, Order order);

	public float[] getData(float[] dataArray, Order order);

	public double[] getData(double[] dataArray, Order order);

	public int[] getIntData(Order order);

	public long[] getLongData(Order order);

	public float[] getFloatData(Order order);

	public double[] getDoubleData(Order order);

	public int[] getData(int[] dataArray);

	public long[] getData(long[] dataArray);

	public float[] getData(float[] dataArray);

	public double[] getData(double[] dataArray);

	public int[] getIntData();

	public long[] getLongData();

	public float[] getFloatData();

	public double[] getDoubleData();

	public int[][] getData2(int[][] dataArray, Order order);

	public long[][] getData2(long[][] dataArray, Order order);

	public float[][] getData2(float[][] dataArray, Order order);

	public double[][] getData2(double[][] dataArray, Order order);

	public int[][] getIntData2(Order order);

	public long[][] getLongData2(Order order);

	public float[][] getFloatData2(Order order);

	public double[][] getDoubleData2(Order order);

	public int[][] getData2(int[][] dataArray);

	public long[][] getData2(long[][] dataArray);

	public float[][] getData2(float[][] dataArray);

	public double[][] getData2(double[][] dataArray);

	public int[][] getIntData2();

	public long[][] getLongData2();

	public float[][] getFloatData2();

	public double[][] getDoubleData2();

	public S operateOnData(Function<? super V, ? extends V> operator);

	public <I> S operateOnData(Order order, List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public <I> S operateOnData(List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public <I> S operateOnData2(Order order,
			List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public <I> S operateOnData2(List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator);

	public <I> S setData(Order order, List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator);

	public <I> S setData(List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator);

	public <I> S setData2(Order order,
			List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator);

	public <I> S setData2(List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator);

	public S setData(Order order, Number... to);

	public S setData(Number... to);

	public S setData(Order order, @ReadOnly Value<?>... to);

	public S setData(@ReadOnly Value<?>... to);

	public S setData2(Order order,
			List<? extends List<? extends @ReadOnly Value<?>>> to);

	public S setData2(List<? extends List<? extends @ReadOnly Value<?>>> to);

	public S setData(Order order, List<? extends @ReadOnly Value<?>> to);

	public S setData(List<? extends @ReadOnly Value<?>> to);

	public S setData(boolean setByReference, Order order,
			@SuppressWarnings("unchecked") V... to);

	public S setData(boolean setByReference,
			@SuppressWarnings("unchecked") V... to);

	public S setData2(boolean setByReference, Order order,
			List<? extends List<? extends V>> to);

	public S setData2(boolean setByReference, List<? extends List<? extends V>> to);

	public S setData(boolean setByReference, Order order, List<? extends V> to);

	public S setData(boolean setByReference, List<? extends V> to);

	public S setData(Order order, int[] to);

	public S setData(Order order, long[] to);

	public S setData(Order order, float[] to);

	public S setData(Order order, double[] to);

	public S setData(int[] to);

	public S setData(long[] to);

	public S setData(float[] to);

	public S setData(double[] to);

	public S setData2(Order order, int[]... to);

	public S setData2(Order order, long[]... to);

	public S setData2(Order order, float[]... to);

	public S setData2(Order order, double[]... to);

	public S setData2(int[]... to);

	public S setData2(long[]... to);

	public S setData2(float[]... to);

	public S setData2(double[]... to);

	@SuppressWarnings("unchecked")
	public S setData2(boolean setByReference, Order order, Vector<?, V>... to);

	@SuppressWarnings("unchecked")
	public S setData2(boolean setByReference, Vector<?, V>... values);

	public S setData2(Order order, Vector<?, ?>... values);

	public S setData2(Vector<?, ?>... values);
}