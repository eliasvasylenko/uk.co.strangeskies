package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.expression.ConditionalExpression;
import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.FunctionExpression;
import uk.co.strangeskies.mathematics.expression.IdentityExpression;
import uk.co.strangeskies.mathematics.expression.collection.ListExpressionView;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.utilities.factory.Factory;
import uk.co.strangeskies.utilities.function.collection.ListTransformOnceView;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

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
		extends CompoundExpression<S> implements Vector<S, V>,
		CopyDecouplingExpression<S> {
	private List<V> data;

	private WeakReference<List<List<V>>> data2Reference;

	private IdentityExpression<Orientation> orientation;
	private Order order;

	/* All constructors must go through here */
	private VectorImpl(Order order, Orientation orientation) {
		if (order == null || orientation == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();
		data2Reference = new WeakReference<List<List<V>>>(null);

		this.order = order;
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
	public final S setData(boolean setByReference, Vector<?, V> to) {
		return setData(setByReference, to.getData());
	}

	@Override
	public final S setData(Vector<?, ?> to) {
		return setData(to.getData());
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

	protected final S transposeImplementation() {
		if (getOrientation() == Orientation.Column) {
			setOrientationImplementation(Orientation.Row);
		} else {
			setOrientationImplementation(Orientation.Column);
		}

		return getThis();
	}

	@Override
	public final Order getOrder() {
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
	public List<List<V>> getData2() {
		List<List<V>> data2 = data2Reference.get();

		if (data2 == null) {
			Expression</* @ReadOnly */Boolean> isOrientationAlignedWithOrder = new FunctionExpression<Orientation, Boolean>(
					getOrientationExpression(),
					orientation -> order == orientation.getAssociatedOrder());

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
					input -> new AbstractList<V>() {
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
