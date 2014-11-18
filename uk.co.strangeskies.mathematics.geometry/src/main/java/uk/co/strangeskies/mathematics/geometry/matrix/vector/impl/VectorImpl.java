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
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.ReOrderedMatrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;
import uk.co.strangeskies.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.utilities.factory.Factory;
import uk.co.strangeskies.utilities.function.TriFunction;
import uk.co.strangeskies.utilities.function.collection.ListTransformOnceView;

/**
 *
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          See {@link Self}.
 * @param <V>
 *          The type of {@link Value} this Vector operates on.
 */
public abstract class VectorImpl<S extends Vector<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Vector<S, V>,
		CopyDecouplingExpression<S> {
	private final List<V> data;
	private final Order order;

	private WeakReference<List<List<V>>> data2Reference;

	private Orientation orientation;

	private VectorImpl(Order order, Orientation orientation) {
		super(false);

		if (order == null || orientation == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();
		data2Reference = new WeakReference<List<List<V>>>(null);

		this.order = order;
		this.orientation = orientation;
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

		getDependencies().addAll(getData());
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

		getDependencies().addAll(getData());
	}

	@Override
	public final String toString() {
		return "["
				+ data.stream().map(Object::toString).collect(Collectors.joining(", "))
				+ "]";
	}

	@Override
	public final int getDimensions() {
		return getData().size();
	}

	@Override
	public final List<V> getData() {
		return Collections.unmodifiableList(data);
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
	public Matrix<?, V> withOrder(Order order) {
		if (order == getOrder())
			return this;
		else
			return new ReOrderedMatrix<V>(this);
	}

	@Override
	public Matrix<?, V> getTransposed() {
		// TODO Auto-generated method stub
		return null;
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
	public final S operateOnData(Function<? super V, ? extends V> operator) {
		getLock().writeLock().lock();

		for (V element : data)
			element = operator.apply(element);

		getDependencies().set(getData());

		getLock().writeLock().unlock();

		return getThis();
	}

	@Override
	public final S operateOnData(
			BiFunction<? super V, Integer, ? extends V> operator) {
		getLock().writeLock().lock();

		int i = 0;
		for (V element : data)
			element = operator.apply(element, i++);

		getDependencies().set(getData());

		getLock().writeLock().unlock();

		return getThis();
	}

	@Override
	public final S operateOnData2(
			TriFunction<? super V, Integer, Integer, ? extends V> operator) {
		getLock().writeLock().lock();

		int i = 0;
		int j = 0;
		if (getOrder() == getOrientation().getAssociatedOrder())
			for (V element : data)
				element = operator.apply(element, i++, j);
		else
			for (V element : data)
				element = operator.apply(element, i, j++);

		getDependencies().set(getData());

		getLock().writeLock().unlock();

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
		getLock().readLock().lock();
		getLock().readLock().unlock();
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
	public final Order getOrder() {
		return order;
	}

	@Override
	public final Orientation getOrientation() {
		return orientation;
	}

	@Override
	public final VectorN<V> getMajorVector(int index) {
		if (index != 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return new VectorNImpl<V>(getOrder(), getOrientation(), data);
	}

	@Override
	public final VectorN<V> getMinorVector(int index) {
		return new VectorNImpl<V>(getOrder(), getOrientation().getOther(),
				Arrays.asList(data.get(index)));
	}

	@Override
	public List<List<V>> getData2() {
		List<List<V>> data2 = data2Reference.get();

		if (data2 == null) {
			if (order != orientation.getAssociatedOrder())
				data2 = new AbstractList<List<V>>() {
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
			else
				data2 = new ListTransformOnceView<>(data,
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

			data2Reference = new WeakReference<>(data2);
		}

		return data2;
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
