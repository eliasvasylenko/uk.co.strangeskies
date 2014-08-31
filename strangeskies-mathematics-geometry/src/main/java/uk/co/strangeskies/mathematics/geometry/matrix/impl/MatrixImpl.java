package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.collection.MergeIndicesListView;
import uk.co.strangeskies.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.utilities.factory.Factory;
import uk.co.strangeskies.utilities.function.collection.ListTransformationView;

public abstract class MatrixImpl<S extends Matrix<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Matrix<S, V>,
		CopyDecouplingExpression<S> {
	private List<List<V>> data;

	private Order order;

	/* All constructors must go through here */
	private MatrixImpl(Order order) {
		if (order == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();

		this.order = order;
	}

	public MatrixImpl(int rows, int columns, Order order, Factory<V> valueFactory) {
		this(order);

		try {
			if (valueFactory == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkValid(rows);
			DimensionalityException.checkValid(columns);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int major;
		int minor;
		if (order == Order.RowMajor) {
			major = columns;
			minor = rows;
		} else {
			major = rows;
			minor = columns;
		}

		for (int i = 0; i < major; i++) {
			List<V> elements = new ArrayList<V>();
			data.add(elements);
			for (int j = 0; j < minor; j++) {
				elements.add(valueFactory.create());
			}
		}

		getDependencies().set(getData());
	}

	public MatrixImpl(Order order, List<? extends List<? extends V>> values) {
		this(order);

		try {
			if (values == null || order == null) {
				throw new NullPointerException();
			}
			NullPointerInCollectionException.checkList(values);

			DimensionalityException.checkValid(values.size());

			Iterator<? extends List<? extends V>> majorIterator = values.iterator();

			List<? extends V> firstMajor = majorIterator.next();
			DimensionalityException.checkValid(firstMajor.size());

			NullPointerInCollectionException.checkList(firstMajor);

			while (majorIterator.hasNext()) {
				List<? extends V> major = majorIterator.next();
				DimensionalityException.checkEquivalence(firstMajor.size(),
						major.size());

				NullPointerInCollectionException.checkList(major);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (List<? extends V> major : values) {
			List<V> elements = new ArrayList<V>();
			data.add(elements);
			for (V value : major) {
				elements.add(value);
			}
		}

		getDependencies().set(getData());
	}

	@Override
	public final Order getOrder() {
		return order;
	}

	/**
	 * Not public by default, as we don't want e.g. a Matrix23 to be able to
	 * transpose the data without also changing order such that it conceptually
	 * remains a 2x3 matrix, as opposed to a 3x2 matrix.
	 */
	protected final S transposeImplementation() {
		List<List<V>> transposedData = new ArrayList<List<V>>();
		for (int i = 0; i < getMajorSize(); i++) {
			List<V> elements = new ArrayList<V>();
			transposedData.add(elements);
			for (int j = 0; j < getMinorSize(); j++) {
				elements.add(data.get(j).get(i));
			}
		}
		data.clear();
		data.addAll(transposedData);

		update();

		return getThis();
	}

	/**
	 * Get the dimensions as: [columns, rows].
	 *
	 * @return A vector representing the current dimensions.
	 */
	@Override
	public Vector2<IntValue> getDimensions2() {
		return new Vector2Impl<IntValue>(Order.ColumnMajor, Orientation.Column,
				IntValue.factory()).setData(getRowSize(), getColumnSize());
	}

	@Override
	public final int getMajorSize() {
		return data.get(0).size();
	}

	@Override
	public final int getMinorSize() {
		return data.size();
	};

	protected List<V> getRowVectorData(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectorData(row);
		} else {
			return getMinorVectorData(row);
		}
	}

	protected List<V> getColumnVectorData(int column) {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVectorData(column);
		} else {
			return getMinorVectorData(column);
		}
	}

	@Override
	public Vector<?, V> getMajorVector(int index) {
		return new VectorNImpl<V>(getOrder(),
				getOrder().getAssociatedOrientation(), getMajorVectorData(index));
	}

	protected List<V> getMajorVectorData(int index) {
		return data.get(index);
	}

	@Override
	public Vector<?, V> getMinorVector(int index) {
		return new VectorNImpl<V>(getOrder(), getOrder().getOther()
				.getAssociatedOrientation(), getMinorVectorData(index));
	}

	protected List<V> getMinorVectorData(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : data) {
			minorElements.add(elements.get(index));
		}
		return minorElements;
	}

	@Override
	public final List<List<V>> getData2() {
		return new ListTransformationView<>(data,
				l -> Collections.unmodifiableList(l));
	}

	@Override
	public final List<V> getData() {
		return new MergeIndicesListView<>(data);
	}

	@Override
	public final String toString() {
		String string = new String();

		string += "[";

		boolean first = true;
		for (List<V> elements : data) {
			if (first) {
				first = false;
			} else {
				string += ", ";
			}

			Iterator<V> dataIterator = elements.iterator();
			string += "[" + dataIterator.next();
			while (dataIterator.hasNext()) {
				string += ", " + dataIterator.next();
			}
			string += "]";
		}

		string += "]";

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

		if (!getDimensions2().equals(thatMatrix.getDimensions2())) {
			return false;
		}

		boolean equalOrder = getOrder().equals(thatMatrix.getOrder());

		List<? extends List<? extends Value<?>>> thoseElements = thatMatrix
				.getData2();
		int i = 0;
		for (List<V> elements : data) {
			int j = 0;
			for (V element : elements) {
				boolean equalElement;
				if (equalOrder) {
					equalElement = element.equals(thoseElements.get(i).get(j));
				} else {
					equalElement = element.equals(thoseElements.get(j).get(i));
				}
				if (!equalElement) {
					return false;
				}

				j++;
			}

			i++;
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

		boolean equalOrder = getOrder().equals(other.getOrder());

		List<? extends List<? extends Value<?>>> thoseElements = other.getData2();
		int i = 0;
		for (List<V> elements : data) {
			int j = 0;
			for (V element : elements) {
				if (equalOrder) {
					comparison = element.compareTo(thoseElements.get(i).get(j));
				} else {
					comparison = element.compareTo(thoseElements.get(j).get(i));
				}
				if (comparison != 0)
					return comparison;

				j++;
			}

			i++;
		}

		return 0;
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

		if (order == getOrder()) {
			int i = 0;
			for (List<V> elements : data) {
				for (V element : elements) {
					dataArray[i++] = element.intValue();
				}
			}
		} else {
			int i = 0;
			for (List<V> elements : data) {
				int j = 0;
				for (V element : elements) {
					dataArray[i + j] = element.intValue();
					j += data.size();
				}
				i++;
			}
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

		if (order == getOrder()) {
			int i = 0;
			for (List<V> elements : data) {
				for (V element : elements) {
					dataArray[i++] = element.longValue();
				}
			}
		} else {
			int i = 0;
			for (List<V> elements : data) {
				int j = 0;
				for (V element : elements) {
					dataArray[i + j] = element.longValue();
					j += data.size();
				}
				i++;
			}
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

		if (order == getOrder()) {
			int i = 0;
			for (List<V> elements : data) {
				for (V element : elements) {
					dataArray[i++] = element.floatValue();
				}
			}
		} else {
			int i = 0;
			for (List<V> elements : data) {
				int j = 0;
				for (V element : elements) {
					dataArray[i + j] = element.floatValue();
					j += data.size();
				}
				i++;
			}
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

		if (order == getOrder()) {
			int i = 0;
			for (List<V> elements : data) {
				for (V element : elements) {
					dataArray[i++] = element.doubleValue();
				}
			}
		} else {
			int i = 0;
			for (List<V> elements : data) {
				int j = 0;
				for (V element : elements) {
					dataArray[i + j] = element.doubleValue();
					j += data.size();
				}
				i++;
			}
		}

		return dataArray;
	}

	@Override
	protected final S evaluate() {
		return getThis();
	}

	@Override
	public final int[][] getData2(int[][] dataArray, Order order) {
		try {
			if (dataArray == null || order == null) {
				throw new NullPointerException();
			}
			NullPointerInCollectionException.checkList(dataArray);

			int majorSize;
			int minorSize;

			if (order == getOrder()) {
				majorSize = getMajorSize();
				minorSize = getMinorSize();
			} else {
				majorSize = getMinorSize();
				minorSize = getMajorSize();
			}

			DimensionalityException.checkEquivalence(minorSize, dataArray.length);
			for (int major = 0; major < minorSize; major++) {
				DimensionalityException.checkEquivalence(majorSize,
						dataArray[major].length);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrder()) {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[i][j] = element.intValue();
					j++;
				}
				i++;
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[j][i] = element.intValue();
					j++;
				}
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
			NullPointerInCollectionException.checkList(dataArray);

			int majorSize;
			int minorSize;

			if (order == getOrder()) {
				majorSize = getMajorSize();
				minorSize = getMinorSize();
			} else {
				majorSize = getMinorSize();
				minorSize = getMajorSize();
			}

			DimensionalityException.checkEquivalence(minorSize, dataArray.length);
			for (int major = 0; major < minorSize; major++) {
				DimensionalityException.checkEquivalence(majorSize,
						dataArray[major].length);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrder()) {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[i][j] = element.longValue();
					j++;
				}
				i++;
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[j][i] = element.longValue();
					j++;
				}
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
			NullPointerInCollectionException.checkList(dataArray);

			int majorSize;
			int minorSize;

			if (order == getOrder()) {
				majorSize = getMajorSize();
				minorSize = getMinorSize();
			} else {
				majorSize = getMinorSize();
				minorSize = getMajorSize();
			}

			DimensionalityException.checkEquivalence(minorSize, dataArray.length);
			for (int major = 0; major < minorSize; major++) {
				DimensionalityException.checkEquivalence(majorSize,
						dataArray[major].length);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrder()) {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[i][j] = element.floatValue();
					j++;
				}
				i++;
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[j][i] = element.floatValue();
					j++;
				}
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
			NullPointerInCollectionException.checkList(dataArray);

			int majorSize;
			int minorSize;

			if (order == getOrder()) {
				majorSize = getMajorSize();
				minorSize = getMinorSize();
			} else {
				majorSize = getMinorSize();
				minorSize = getMajorSize();
			}

			DimensionalityException.checkEquivalence(minorSize, dataArray.length);
			for (int major = 0; major < minorSize; major++) {
				DimensionalityException.checkEquivalence(majorSize,
						dataArray[major].length);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		if (order == getOrder()) {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[i][j] = element.doubleValue();
					j++;
				}
				i++;
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					dataArray[j][i] = element.doubleValue();
					j++;
				}
				i++;
			}
		}

		return dataArray;
	}

	@Override
	public V getElement(int major, int minor) {
		return data.get(major).get(minor);
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

		if (order == this.getOrder()) {
			Iterator<? extends I> itemIterator = itemList.iterator();

			for (List<V> major : data) {
				for (V element : major) {
					element = operator.apply(element, itemIterator.next());
				}
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					element = operator.apply(element,
							itemList.get(i + j * getMajorSize()));
					j++;
				}
				i++;
			}
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

			int majorSize;
			int minorSize;

			if (order == getOrder()) {
				majorSize = getMajorSize();
				minorSize = getMinorSize();
			} else {
				majorSize = getMinorSize();
				minorSize = getMajorSize();
			}

			DimensionalityException.checkEquivalence(minorSize, itemList.size());
			for (int major = 0; major < minorSize; major++) {
				DimensionalityException.checkEquivalence(majorSize, itemList.get(major)
						.size());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		getDependencies().clear();

		if (order == this.getOrder()) {
			Iterator<? extends List<? extends I>> itemListIterator = itemList
					.iterator();

			for (List<V> major : data) {
				Iterator<? extends I> itemIterator = itemListIterator.next().iterator();

				for (V element : major) {
					element = operator.apply(element, itemIterator.next());
				}
			}
		} else {
			int i = 0;
			for (List<V> major : data) {
				int j = 0;
				for (V element : major) {
					element = operator.apply(element, itemList.get(j).get(i));
					j++;
				}
				i++;
			}
		}

		getDependencies().addAll(getData());

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

		getDependencies().clear();

		for (List<V> major : data) {
			for (V element : major) {
				element = operator.apply(element);
			}
		}

		getDependencies().addAll(getData());

		return getThis();
	}
}
