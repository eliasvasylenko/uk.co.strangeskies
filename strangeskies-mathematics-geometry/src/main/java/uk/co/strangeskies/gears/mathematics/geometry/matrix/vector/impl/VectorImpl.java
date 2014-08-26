package uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.gears.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.gears.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.gears.mathematics.expression.ConditionalExpression;
import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.expression.Expression;
import uk.co.strangeskies.gears.mathematics.expression.IdentityExpression;
import uk.co.strangeskies.gears.mathematics.expression.collection.ListExpressionView;
import uk.co.strangeskies.gears.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.values.DoubleArrayListView;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.DoubleValueFactory;
import uk.co.strangeskies.gears.mathematics.values.FloatArrayListView;
import uk.co.strangeskies.gears.mathematics.values.FloatValue;
import uk.co.strangeskies.gears.mathematics.values.FloatValueFactory;
import uk.co.strangeskies.gears.mathematics.values.IntArrayListView;
import uk.co.strangeskies.gears.mathematics.values.IntValue;
import uk.co.strangeskies.gears.mathematics.values.IntValueFactory;
import uk.co.strangeskies.gears.mathematics.values.LongArrayListView;
import uk.co.strangeskies.gears.mathematics.values.LongValue;
import uk.co.strangeskies.gears.mathematics.values.LongValueFactory;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.Self;
import uk.co.strangeskies.gears.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.gears.utilities.function.AssignmentOperation;
import uk.co.strangeskies.gears.utilities.function.collection.ListTransformOnceView;
import uk.co.strangeskies.gears.utilities.function.collection.ListTransformationView;

/**
 * 
 * @author Elias N Vasylenko
 * 
 * @param <S>
 *          See {@link Self}.
 * @param <C>
 *          See {@link Const}.
 * @param <V>
 *          The type of {@link Value} this Vector operates on.
 */
public abstract class VectorImpl<S extends Vector<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Vector<S, V>, CopyDecouplingExpression<S> {
	private List<V> data;

	private WeakReference<List<List<V>>> data2Reference;

	private IdentityExpression<Orientation> orientation;
	private IdentityExpression<Order> order;

	/* All constructors must go through here */
	private VectorImpl(Order order, Orientation orientation) {
		if (order == null || orientation == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();
		data2Reference = new WeakReference<List<List<V>>>(null);

		this.order = new IdentityExpression<>(order);
		this.orientation = new IdentityExpression<>(orientation);
	}

	public VectorImpl(int size, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		this(order, orientation);

		try {
			if (valueFactory == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkValid(size);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (int i = 0; i < size; i++) {
			data.add(valueFactory.create());
		}

		finalise();
	}

	public VectorImpl(Order order, Orientation orientation,
			List<? extends V> values) {
		this(order, orientation);

		try {
			if (values == null || order == null || orientation == null) {
				throw new NullPointerException();
			}
			NullPointerInCollectionException.checkList(values);

			DimensionalityException.checkValid(values.size());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (V value : values) {
			data.add(value);
		}

		finalise();
	}

	@Override
	public final S get() {
		return getThis();
	}

	@Override
	public final S setOrder(Order order) {
		return setOrderImplementation(order);
	}

	@Override
	public final S transpose() {
		return transposeImplementation();
	}

	protected void finalise() {
		getDependencies().addAll(getData());
	}

	@Override
	public final int getDimensions() {
		return getData().size();
	}

	protected final S resizeImplementation(int dimensions) {
		try {
			DimensionalityException.checkValid(dimensions);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		while (dimensions < getDimensions()) {
			getDependencies().remove(data.remove(data.size() - 1));
		}
		while (dimensions > getDimensions()) {
			V element = data.get(0).copy().setValue(0);
			data.add(element);

			getDependencies().add(element);
		}

		return getThis();
	}

	@Override
	public final List<V> getData() {
		return Collections.<V> unmodifiableList(data);
	}

	@Override
	public S multiply(Value<?> scalar) {
		for (V value : data) {
			value.multiply(scalar);
		}
		return getThis();
	}

	@Override
	public S multiply(int scalar) {
		for (V value : data) {
			value.multiply(scalar);
		}
		return getThis();
	}

	@Override
	public S multiply(long scalar) {
		for (V value : data) {
			value.multiply(scalar);
		}
		return getThis();
	}

	@Override
	public S multiply(float scalar) {
		for (V value : data) {
			value.multiply(scalar);
		}
		return getThis();
	}

	@Override
	public S multiply(double scalar) {
		for (V value : data) {
			value.multiply(scalar);
		}
		return getThis();
	}

	@Override
	public final S getMultiplied(Value<?> value) {
		return copy().multiply(value);
	}

	@Override
	public final S getMultiplied(int value) {
		return copy().multiply(value);
	}

	@Override
	public final S getMultiplied(long value) {
		return copy().multiply(value);
	}

	@Override
	public final S getMultiplied(float value) {
		return copy().multiply(value);
	}

	@Override
	public final S getMultiplied(double value) {
		return copy().multiply(value);
	}

	@Override
	public S divide(Value<?> scalar) {
		for (V value : data) {
			value.divide(scalar);
		}
		return getThis();
	}

	@Override
	public S divide(int scalar) {
		for (V value : data) {
			value.divide(scalar);
		}
		return getThis();
	}

	@Override
	public S divide(long scalar) {
		for (V value : data) {
			value.divide(scalar);
		}
		return getThis();
	}

	@Override
	public S divide(float scalar) {
		for (V value : data) {
			value.divide(scalar);
		}
		return getThis();
	}

	@Override
	public S divide(double scalar) {
		for (V value : data) {
			value.divide(scalar);
		}
		return getThis();
	}

	@Override
	public final S getDivided(Value<?> value) {
		return copy().divide(value);
	}

	@Override
	public final S getDivided(int value) {
		return copy().divide(value);
	}

	@Override
	public final S getDivided(long value) {
		return copy().divide(value);
	}

	@Override
	public final S getDivided(float value) {
		return copy().divide(value);
	}

	@Override
	public final S getDivided(double value) {
		return copy().divide(value);
	}

	@Override
	public S add(Matrix<?, ?> other) {
		operateOnData2(other.getOrder(), other.getData2(),
				new BiFunction<V, Value<?>, V>() {
					@Override
					public V apply(V firstOperand, Value<?> secondOperand) {
						return firstOperand.add(secondOperand);
					}
				});

		return getThis();
	}

	@Override
	public final S getAdded(Matrix<?, ?> value) {
		return copy().add(value);
	}

	@Override
	public S subtract(Matrix<?, ?> other) {
		operateOnData2(other.getOrder(), other.getData2(),
				new BiFunction<V, Value<?>, V>() {
					@Override
					public V apply(V firstOperand, Value<?> secondOperand) {
						return firstOperand.subtract(secondOperand);
					}
				});

		return getThis();
	}

	@Override
	public final S getSubtracted(Matrix<?, ?> value) {
		return copy().subtract(value);
	}

	@Override
	public S multiply(Matrix<?, ?> other) {
		setData(multiplyData(other.getData2()));

		return getThis();
	}

	@Override
	public S preMultiply(Matrix<?, ?> other) {
		setData(preMultiplyData(other.getData2()));

		return getThis();
	}

	protected final List<Value<V>> multiplyData(
			List<? extends List<? extends Value<?>>> otherData) {
		// TODO implement multiplication! include isResiseable() in parameter
		// dimensions check...

		return null;
	}

	protected final List<Value<V>> preMultiplyData(
			List<? extends List<? extends Value<?>>> otherData) {
		// TODO as above.

		return null;
	}

	@Override
	public final S getMultiplied(Matrix<?, ?> value) {
		return copy().multiply(value);
	}

	@Override
	public final S getPreMultiplied(Matrix<?, ?> value) {
		return copy().preMultiply(value);
	}

	@Override
	public final S negate() {
		for (V value : data) {
			value.negate();
		}
		return getThis();
	}

	@Override
	public final S getNegated() {
		return copy().negate();
	}

	@Override
	public final String toString() {
		String string = new String();

		string += "(";

		boolean first = true;
		for (V element : data) {
			if (first) {
				first = false;
			} else {
				string += ", ";
			}

			string += element;
		}

		string += ")";

		return string;
	}

	@Override
	public final boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof Matrix<?, ?>)) {
			return false;
		}

		Matrix<?, ?> thatMatrix = (Matrix<?, ?>) that;

		if (getRowSize() != thatMatrix.getRowSize()
				|| getColumnSize() != thatMatrix.getColumnSize()) {
			return false;
		}

		Iterator<? extends Value<?>> otherIterator = thatMatrix.getData()
				.iterator();
		for (V element : data) {
			if (!element.equals(otherIterator.next()))
				return false;
		}

		return true;
	}

	@Override
	public final int hashCode() {
		return getRowSize() + getColumnSize() + getOrder().hashCode()
				+ data.hashCode();
	}

	@Override
	public final int compareTo(Matrix<?, ?> other) {
		int comparison;

		comparison = getDimensions2().compareTo(other.getDimensions2());
		if (comparison != 0)
			return comparison;

		Iterator<? extends Value<?>> otherIterator = other.getData().iterator();
		for (V element : data) {
			comparison = element.compareTo(otherIterator.next());
			if (comparison != 0)
				return comparison;
		}

		return 0;
	}

	public static <T extends Vector<?, ?>> T assertDimensions(T matrix, int size) {
		try {
			DimensionalityException.checkEquivalence(matrix.getDimensions(), size);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		return matrix;
	}

	@Override
	public final int[] getData(int[] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkEquivalence(getDimensions2().getX()
					.getMultiplied(getDimensions2().getY()).intValue(), dataArray.length);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int i = 0;
		for (V element : data) {
			dataArray[i++] = element.intValue();
		}

		return dataArray;
	}

	@Override
	public final long[] getData(long[] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkEquivalence(getDimensions2().getX()
					.getMultiplied(getDimensions2().getY()).intValue(), dataArray.length);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int i = 0;
		for (V element : data) {
			dataArray[i++] = element.longValue();
		}

		return dataArray;
	}

	@Override
	public final float[] getData(float[] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkEquivalence(getDimensions2().getX()
					.getMultiplied(getDimensions2().getY()).intValue(), dataArray.length);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int i = 0;
		for (V element : data) {
			dataArray[i++] = element.floatValue();
		}

		return dataArray;
	}

	@Override
	public final double[] getData(double[] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkEquivalence(getDimensions2().getX()
					.getMultiplied(getDimensions2().getY()).intValue(), dataArray.length);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int i = 0;
		for (V element : data) {
			dataArray[i++] = element.doubleValue();
		}

		return dataArray;
	}

	@Override
	public final int[] getIntData(Order order) {
		return getData(new int[getDataSize()], order);
	}

	@Override
	public final long[] getLongData(Order order) {
		return getData(new long[getDataSize()], order);
	}

	@Override
	public final float[] getFloatData(Order order) {
		return getData(new float[getDataSize()], order);
	}

	@Override
	public final double[] getDoubleData(Order order) {
		return getData(new double[getDataSize()], order);
	}

	@Override
	public final int[] getData(int[] dataArray) {
		return getData(dataArray, getOrder());
	}

	@Override
	public final long[] getData(long[] dataArray) {
		return getData(dataArray, getOrder());
	}

	@Override
	public final float[] getData(float[] dataArray) {
		return getData(dataArray, getOrder());
	}

	@Override
	public final double[] getData(double[] dataArray) {
		return getData(dataArray, getOrder());
	}

	@Override
	public final int[] getIntData() {
		return getIntData(getOrder());
	}

	@Override
	public final long[] getLongData() {
		return getLongData(getOrder());
	}

	@Override
	public final float[] getFloatData() {
		return getFloatData(getOrder());
	}

	@Override
	public final double[] getDoubleData() {
		return getDoubleData(getOrder());
	}

	@Override
	public final int[][] getData2(int[][] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrientation().getAssociatedOrder()) {
			try {
				DimensionalityException.checkEquivalence(dataArray.length, 1);

				int[] major = dataArray[0];

				DimensionalityException.checkEquivalence(major.length, getDimensions());
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[0][i] = element.intValue();
				i++;
			}
		} else {
			try {
				DimensionalityException.checkEquivalence(dataArray.length,
						getDimensions());

				for (int[] major : dataArray) {
					DimensionalityException.checkEquivalence(major.length, 1);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[i][0] = element.intValue();
				i++;
			}
		}

		return dataArray;
	}

	@Override
	public final long[][] getData2(long[][] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrientation().getAssociatedOrder()) {
			try {
				DimensionalityException.checkEquivalence(dataArray.length, 1);

				long[] major = dataArray[0];

				DimensionalityException.checkEquivalence(major.length, getDimensions());
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[0][i] = element.longValue();
				i++;
			}
		} else {
			try {
				DimensionalityException.checkEquivalence(dataArray.length,
						getDimensions());

				for (long[] major : dataArray) {
					DimensionalityException.checkEquivalence(major.length, 1);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[i][0] = element.longValue();
				i++;
			}
		}

		return dataArray;
	}

	@Override
	public final float[][] getData2(float[][] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrientation().getAssociatedOrder()) {
			try {
				DimensionalityException.checkEquivalence(dataArray.length, 1);

				float[] major = dataArray[0];

				DimensionalityException.checkEquivalence(major.length, getDimensions());
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[0][i] = element.floatValue();
				i++;
			}
		} else {
			try {
				DimensionalityException.checkEquivalence(dataArray.length,
						getDimensions());

				for (float[] major : dataArray) {
					DimensionalityException.checkEquivalence(major.length, 1);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[i][0] = element.floatValue();
				i++;
			}
		}

		return dataArray;
	}

	@Override
	public final double[][] getData2(double[][] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrientation().getAssociatedOrder()) {
			try {
				DimensionalityException.checkEquivalence(dataArray.length, 1);

				double[] major = dataArray[0];

				DimensionalityException.checkEquivalence(major.length, getDimensions());
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[0][i] = element.doubleValue();
				i++;
			}
		} else {
			try {
				DimensionalityException.checkEquivalence(dataArray.length,
						getDimensions());

				for (double[] major : dataArray) {
					DimensionalityException.checkEquivalence(major.length, 1);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			int i = 0;
			for (V element : data) {
				dataArray[i][0] = element.doubleValue();
				i++;
			}
		}

		return dataArray;
	}

	@Override
	public final int[][] getIntData2(Order order) {
		return getData2(new int[getMajorSize(order)][getMinorSize(order)], order);
	}

	@Override
	public final long[][] getLongData2(Order order) {
		return getData2(new long[getMajorSize(order)][getMinorSize(order)], order);
	}

	@Override
	public final float[][] getFloatData2(Order order) {
		return getData2(new float[getMajorSize(order)][getMinorSize(order)], order);
	}

	@Override
	public final double[][] getDoubleData2(Order order) {
		return getData2(new double[getMajorSize(order)][getMinorSize(order)], order);
	}

	@Override
	public final int[][] getData2(int[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	@Override
	public final long[][] getData2(long[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	@Override
	public final float[][] getData2(float[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	@Override
	public final double[][] getData2(double[][] dataArray) {
		return getData2(dataArray, getOrder());
	}

	@Override
	public final int[][] getIntData2() {
		return getIntData2(getOrder());
	}

	@Override
	public final long[][] getLongData2() {
		return getLongData2(getOrder());
	}

	@Override
	public final float[][] getFloatData2() {
		return getFloatData2(getOrder());
	}

	@Override
	public final double[][] getDoubleData2() {
		return getDoubleData2(getOrder());
	}

	@Override
	public V getElement(int major, int minor) {
		int index;

		if (getOrder() == getOrientation().getAssociatedOrder()) {
			if (minor != 1) {
				new ArrayIndexOutOfBoundsException();
			}
			index = major;
		} else {
			if (major != 1) {
				new ArrayIndexOutOfBoundsException();
			}
			index = minor;
		}

		return data.get(index);
	}

	@Override
	public final V getElement(int index) {
		return data.get(index);
	}

	@Override
	public final S set(Matrix<?, ?> to) {
		setOrderImplementation(to.getOrder(), false);

		return setData2(to.getData2());
	}

	@Override
	public final <I> S operateOnData(Order order, List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		try {
			if (operator == null || order == null || itemList == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkEquivalence(getMajorSize() * getMinorSize(),
					itemList.size());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		getDependencies().clear();

		Iterator<? extends I> itemIterator = itemList.iterator();

		for (V element : data) {
			element = operator.apply(element, itemIterator.next());
		}

		getDependencies().addAll(getData());

		return getThis();
	}

	@Override
	public final <I> S operateOnData(List<? extends I> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData(getOrder(), itemList, operator);
	}

	@Override
	public final <I> S operateOnData2(Order order,
			List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {

		try {
			if (operator == null || order == null || itemList == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrientation().getAssociatedOrder()) {
			try {
				DimensionalityException.checkEquivalence(itemList.size(), 1);

				List<? extends I> major = itemList.get(0);

				if (major == null) {
					throw new NullPointerException();
				}

				DimensionalityException.checkEquivalence(major.size(), getDimensions());

				NullPointerInCollectionException.checkList(major);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			Iterator<? extends I> itemIterator = itemList.get(0).iterator();

			for (V element : data) {
				element = operator.apply(element, itemIterator.next());
			}

			getDependencies().set(getData());
		} else {
			try {
				DimensionalityException.checkEquivalence(itemList.size(),
						getDimensions());

				NullPointerInCollectionException.checkList(itemList);

				for (List<? extends I> major : itemList) {
					DimensionalityException.checkEquivalence(major.size(), 1);

					if (major.get(0) == null) {
						throw new NullPointerException();
					}
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}

			Iterator<? extends List<? extends I>> itemIterator = itemList.iterator();

			for (V element : data) {
				element = operator.apply(element, itemIterator.next().get(0));
			}

			getDependencies().set(getData());
		}

		return getThis();
	}

	@Override
	public final <I> S operateOnData2(List<? extends List<? extends I>> itemList,
			BiFunction<? super V, ? super I, ? extends V> operator) {
		return operateOnData2(getOrder(), itemList, operator);
	}

	@Override
	public final S operateOnData(Function<? super V, ? extends V> operator) {
		try {
			if (operator == null) {
				throw new NullPointerException();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (V element : data) {
			element = operator.apply(element);
		}

		getDependencies().set(getData());

		return getThis();
	}

	@Override
	public final <I> S setData(Order order, List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return operateOnData(order, itemList, operator);
	}

	@Override
	public final <I> S setData(List<? extends I> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return setData(getOrder(), itemList, operator);
	}

	@Override
	public final <I> S setData2(Order order,
			List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return operateOnData2(order, itemList, operator);
	}

	@Override
	public final <I> S setData2(List<? extends List<? extends I>> itemList,
			AssignmentOperation<V, ? super I> operator) {
		return setData2(getOrder(), itemList, operator);
	}

	@Override
	public final S setData2(final boolean setByReference, Order order,
			List<? extends List<? extends V>> to) {
		if (setByReference) {
			return operateOnData2(order, to, new BiFunction<V, V, V>() {
				@Override
				public V apply(V firstOperand, V secondOperand) {
					return secondOperand;
				}
			});
		} else {
			return operateOnData2(order, to, new BiFunction<V, V, V>() {
				@Override
				public V apply(V firstOperand, V secondOperand) {
					return firstOperand.setValue(secondOperand);
				}
			});
		}
	}

	@Override
	public final S setData2(boolean setByReference,
			List<? extends List<? extends V>> to) {
		return setData2(setByReference, getOrder(), to);
	}

	@Override
	public final S setData(final boolean setByReference, Order order,
			List<? extends V> to) {
		if (setByReference) {
			return operateOnData(order, to, new BiFunction<V, V, V>() {
				@Override
				public V apply(V firstOperand, V secondOperand) {
					if (secondOperand == null) {
						throw new IllegalArgumentException(new NullPointerException());
					}

					return secondOperand;
				}
			});
		} else {
			return operateOnData(order, to, new BiFunction<V, V, V>() {
				@Override
				public V apply(V firstOperand, V secondOperand) {
					if (secondOperand == null) {
						throw new IllegalArgumentException(new NullPointerException());
					}

					return firstOperand.setValue(secondOperand);
				}
			});
		}
	}

	@Override
	public final S setData(boolean setByReference, List<? extends V> to) {
		return setData(setByReference, getOrder(), to);
	}

	@Override
	public final S setData(boolean setByReference, Vector<?, V> to) {
		return setData(setByReference, to.getData());
	}

	@Override
	public final S setData(Vector<?, ?> to) {
		return setData(to.getData());
	}

	@Override
	@SuppressWarnings("unchecked")
	public final S setData(boolean setByReference, Order order, V... to) {
		return setData(setByReference, order, Arrays.asList(to));
	}

	@Override
	@SuppressWarnings("unchecked")
	public final S setData(boolean setByReference, V... to) {
		return setData(setByReference, Arrays.asList(to));
	}

	@Override
	public final S setData(Order order, Number... values) {
		return setData(order, Arrays.asList(values),
				new AssignmentOperation<V, Number>() {
					@Override
					public void assign(V assignee, Number assignment) {
						if (assignment == null) {
							throw new IllegalArgumentException(new NullPointerException());
						}
						assignee.setValue(assignment);
					}
				});
	}

	@Override
	public final S setData(Number... values) {
		return setData(getOrder(), values);
	}

	@Override
	public final S setData(Order order, Value<?>... to) {
		return setData(order, Arrays.asList(to));
	}

	@Override
	public final S setData(Value<?>... to) {
		return setData(Arrays.asList(to));
	}

	@Override
	public final S setData(Order order, int[] to) {
		return setData(order,
				new IntArrayListView<>(to, IntValueFactory.instance()));
	}

	@Override
	public final S setData(Order order, long[] to) {
		return setData(order,
				new LongArrayListView<>(to, LongValueFactory.instance()));
	}

	@Override
	public final S setData(Order order, float[] to) {
		return setData(order,
				new FloatArrayListView<>(to, FloatValueFactory.instance()));
	}

	@Override
	public final S setData(Order order, double[] to) {
		return setData(order,
				new DoubleArrayListView<>(to, DoubleValueFactory.instance()));
	}

	@Override
	public final S setData(int[] to) {
		return setData(getOrder(), to);
	}

	@Override
	public final S setData(long[] to) {
		return setData(getOrder(), to);
	}

	@Override
	public final S setData(float[] to) {
		return setData(getOrder(), to);
	}

	@Override
	public final S setData(double[] to) {
		return setData(getOrder(), to);
	}

	@Override
	public final S setData2(Order order, int[]... to) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(to),
				new Function<int[], List<IntValue>>() {
					@Override
					public List<IntValue> apply(int[] input) {
						return new IntArrayListView<>(input, IntValueFactory.instance());
					};
				}));
	}

	@Override
	public final S setData2(Order order, long[]... to) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(to),
				new Function<long[], List<LongValue>>() {
					@Override
					public List<LongValue> apply(long[] input) {
						return new LongArrayListView<>(input, LongValueFactory.instance());
					};
				}));
	}

	@Override
	public final S setData2(Order order, float[]... to) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(to),
				new Function<float[], List<FloatValue>>() {
					@Override
					public List<FloatValue> apply(float[] input) {
						return new FloatArrayListView<>(input, FloatValueFactory.instance());
					};
				}));
	}

	@Override
	public final S setData2(Order order, double[]... to) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(to),
				new Function<double[], List<DoubleValue>>() {
					@Override
					public List<DoubleValue> apply(double[] input) {
						return new DoubleArrayListView<>(input, DoubleValueFactory
								.instance());
					};
				}));
	}

	@Override
	public final S setData2(int[]... to) {
		return setData2(getOrder(), to);
	}

	@Override
	public final S setData2(long[]... to) {
		return setData2(getOrder(), to);
	}

	@Override
	public final S setData2(float[]... to) {
		return setData2(getOrder(), to);
	}

	@Override
	public final S setData2(double[]... to) {
		return setData2(getOrder(), to);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final S setData2(boolean copyByReference, Order order,
			Vector<?, V>... to) {
		return setData2(copyByReference, order,
				new ListTransformationView<>(Arrays.asList(to),
						new Function<Vector<?, V>, List<? extends V>>() {
							@Override
							public List<? extends V> apply(Vector<?, V> input) {
								return input.getData();
							}
						}));
	}

	@Override
	@SuppressWarnings("unchecked")
	public final S setData2(boolean copyByReference, Vector<?, V>... values) {
		return setData2(getOrder(), values);
	}

	@Override
	public final S setData2(Order order, Vector<?, ?>... values) {
		return setData2(order, new ListTransformationView<>(Arrays.asList(values),
				new Function<Vector<?, ?>, List<? extends Value<?>>>() {
					@Override
					public List<? extends Value<?>> apply(Vector<?, ?> input) {
						return input.getData();
					}
				}));
	}

	@Override
	public final S setData2(Vector<?, ?>... values) {
		return setData2(getOrder(), values);
	}

	@Override
	public final S setData2(Order order,
			List<? extends List<? extends Value<?>>> to) {
		return setData2(order, to, new AssignmentOperation<V, Value<?>>() {
			@Override
			public void assign(V assignee, Value<?> assignment) {
				if (assignment == null) {
					throw new IllegalArgumentException(new NullPointerException());
				}

				assignee.set(assignment);
			}
		});
	}

	@Override
	public final S setData2(List<? extends List<? extends Value<?>>> to) {
		return setData2(getOrder(), to);
	}

	@Override
	public final S setData(Order order, List<? extends Value<?>> to) {
		return setData(order, to, new AssignmentOperation<V, Value<?>>() {
			@Override
			public void assign(V assignee, Value<?> assignment) {
				if (assignment == null) {
					throw new IllegalArgumentException(new NullPointerException());
				}

				assignee.set(assignment);
			}
		});
	}

	@Override
	public final S setData(List<? extends Value<?>> to) {
		return setData(getOrder(), to);
	}

	@Override
	public boolean isResizable() {
		return false;
	}

	@Override
	protected final S evaluate() {
		return getThis();
	}

	@Override
	public Vector2<IntValue> getDimensions2() {
		Vector2<IntValue> dimensions = new Vector2Impl<IntValue>(Order.ColumnMajor,
				Orientation.Column, IntValue.factory());

		if (getOrientation() == Orientation.Column) {
			return dimensions.setData(1, getDimensions());
		} else {
			return dimensions.setData(getDimensions(), 1);
		}
	}

	@Override
	public final int getDataSize() {
		return getDimensions();
	}

	@Override
	public final int getMajorSize() {
		if (getOrientation().getAssociatedOrder() == getOrder()) {
			return getDimensions();
		} else {
			return 1;
		}
	}

	@Override
	public final int getMinorSize() {
		if (getOrientation().getAssociatedOrder() != getOrder()) {
			return getDimensions();
		} else {
			return 1;
		}
	}

	@Override
	public final int getMajorSize(Order order) {
		if (order == getOrder()) {
			return getMajorSize();
		} else {
			return getMinorSize();
		}
	}

	@Override
	public final int getMinorSize(Order order) {
		if (order == getOrder()) {
			return getMinorSize();
		} else {
			return getMajorSize();
		}
	}

	@Override
	public final int getRowSize() {
		if (getOrientation() == Orientation.Row) {
			return getDimensions();
		} else {
			return 1;
		}
	}

	@Override
	public final int getColumnSize() {
		if (getOrientation() == Orientation.Column) {
			return getDimensions();
		} else {
			return 1;
		}
	}

	@Override
	public final boolean isSquare() {
		return getDimensions() == 1;
	}

	protected final S setOrderImplementation(Order order) {
		this.order.set(order);

		return getThis();
	}

	protected final S setOrderImplementation(Order order, boolean transposeData) {
		if (this.order.getValue() != order) {
			this.order.set(order);
			if (transposeData) {
				transposeImplementation();
			}
		}

		return getThis();
	}

	protected final S transposeImplementation() {
		if (getOrientation() == Orientation.Column) {
			setOrientationImplementation(Orientation.Row);
		} else {
			setOrientationImplementation(Orientation.Column);
		}

		return getThis();
	}

	@Override
	public final S getTransposed() {
		return ((VectorImpl<S, V>) copy()).transposeImplementation();
	}

	@Override
	public final Order getOrder() {
		return order.getValue();
	}

	@Override
	public final Expression<Order> getOrderExpression() {
		return order;
	}

	protected final S setOrientationImplementation(Orientation orientation) {
		this.orientation.set(orientation);

		return getThis();
	}

	@Override
	public final Orientation getOrientation() {
		return orientation.getValue();
	}

	@Override
	public final IdentityExpression<Orientation> getOrientationExpression() {
		return orientation;
	}

	@Override
	public final VectorNImpl<V> getRowVector(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVector(row);
		} else {
			return getMinorVector(row);
		}
	}

	@Override
	public final VectorNImpl<V> getColumnVector(int column) {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVector(column);
		} else {
			return getMinorVector(column);
		}
	}

	@Override
	public final VectorNImpl<V> getMajorVector(int index) {
		if (index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return new VectorNImpl<V>(getOrder(), getOrientation(), data);
	}

	@Override
	public final VectorNImpl<V> getMinorVector(int index) {
		return new VectorNImpl<V>(getOrder(), getOrientation().getOther(),
				Arrays.asList(data.get(index)));
	}

	@Override
	public final List<VectorNImpl<V>> getRowVectors() {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	@Override
	public final List<VectorNImpl<V>> getColumnVectors() {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	@Override
	public final List<VectorNImpl<V>> getMajorVectors() {
		return new AbstractList<VectorNImpl<V>>() {
			@Override
			public VectorNImpl<V> get(int index) {
				return getMajorVector(index);
			}

			@Override
			public int size() {
				return getMinorSize();
			}
		};
	}

	@Override
	public final List<VectorNImpl<V>> getMinorVectors() {
		return new AbstractList<VectorNImpl<V>>() {
			@Override
			public VectorNImpl<V> get(int index) {
				return getMinorVector(index);
			}

			@Override
			public int size() {
				return getMajorSize();
			}
		};
	}

	@Override
	public List<List<V>> getData2() {
		List<List<V>> data2 = data2Reference.get();

		if (data2 == null) {
			Expression</* @ReadOnly */Boolean> isOrientationAlignedWithOrder = new BiFunctionExpression</*
																																																				 * @
																																																				 * ReadOnly
																																																				 */Order, Orientation, Boolean>(
					getOrderExpression(), getOrientationExpression(),
					new BiFunction</* @ReadOnly */Order, Orientation, Boolean>() {
						@Override
						public/* @ReadOnly */Boolean apply(Order firstOperand,
								Orientation secondOperand) {
							return firstOperand == secondOperand.getAssociatedOrder();
						}
					});

			List<? extends List<V>> aligned = new AbstractList<List<V>>() {
				@Override
				public List<V> get(int index) {
					if (index != 0) {
						throw new ArrayIndexOutOfBoundsException();
					}
					return data;
				}

				@Override
				public int size() {
					return 1;
				}
			};

			List<? extends List<V>> notAligned = new ListTransformOnceView<>(data,
					new Function<V, List<V>>() {
						@Override
						public List<V> apply(final V input) {
							return new AbstractList<V>() {
								@Override
								public V get(int index) {
									if (index != 0) {
										throw new ArrayIndexOutOfBoundsException();
									}
									return input;
								}

								@Override
								public int size() {
									return 1;
								}
							};
						}
					});

			data2 = new ListExpressionView<>(new ConditionalExpression<>(
					isOrientationAlignedWithOrder, aligned, notAligned));

			data2Reference = new WeakReference<>(data2);
		}

		return data2;
	}

	@Override
	public final S setOrientation(Orientation orientation) {
		return setOrientationImplementation(orientation);
	}

	@Override
	public S translate(Vector<?, ?> translation) {
		add(translation);

		return getThis();
	}

	@Override
	public final S getTranslated(Vector<?, ?> translation) {
		return copy().translate(translation);
	}

	@Override
	public final DoubleValue getSize() {
		return new DoubleValue(getSizeSquared()).squareRoot();
	}

	@Override
	public final V getSizeSquared() {
		Iterator<V> data = getData().iterator();

		V value = data.next().copy().square();

		while (data.hasNext()) {
			value.add(data.next().getSquared());
		}

		return value;
	}
}
