package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.expression.IdentityExpression;
import uk.co.strangeskies.gears.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.VectorNImpl;
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
import uk.co.strangeskies.gears.utilities.collection.MergeIndicesListView;
import uk.co.strangeskies.gears.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.gears.utilities.factory.Factory;
import uk.co.strangeskies.gears.utilities.function.AssignmentOperation;
import uk.co.strangeskies.gears.utilities.function.collection.ListTransformationView;

public abstract class MatrixImpl<S extends Matrix<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Matrix<S, V>,
		CopyDecouplingExpression<S> {
	private List<List<V>> data;

	private IdentityExpression<Order> order;

	/* All constructors must go through here */
	private MatrixImpl(Order order) {
		if (order == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();

		this.order = new IdentityExpression<>(order);
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

		finalise();
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

		finalise();
	}

	protected void finalise() {
		getDependencies().set(getData());
	}

	@Override
	public S get() {
		return getThis();
	}

	@Override
	public final Order getOrder() {
		return order.getValue();
	}

	@Override
	public final IdentityExpression<Order> getOrderExpression() {
		return order;
	}

	@Override
	public final S setOrder(Order order) {
		return setOrder(order, true);
	}

	/**
	 * Not public by default, as we don't want e.g. a Matrix23 to be able to
	 * change order without also transposing the data such that it conceptually
	 * remains a 2x3 matrix, as opposed to a 3x2 matrix.
	 *
	 * @param order
	 * @param transposeData
	 */
	protected final S setOrder(Order order, boolean transposeData) {
		if (this.order.getValue() != order) {
			this.order.set(order);
			if (transposeData) {
				transposeImplementation();
			}
		}

		return getThis();
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

	@Override
	public Matrix<?, V> getTransposed() {
		return new MatrixNImpl<V>(getOrder(), getData2()).transpose();
	}

	@Override
	public final S negate() {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.negate();
			}
		}
		return getThis();
	}

	@Override
	public final S getNegated() {
		return copy().negate();
	}

	@Override
	public final S multiply(Value<?> scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.multiply(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S multiply(int scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.multiply(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S multiply(long scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.multiply(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S multiply(float scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.multiply(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S multiply(double scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.multiply(scalar);
			}
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
	public final S divide(Value<?> scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.divide(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S divide(int scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.divide(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S divide(long scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.divide(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S divide(float scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.divide(scalar);
			}
		}
		return getThis();
	}

	@Override
	public final S divide(double scalar) {
		for (List<V> elements : data) {
			for (V element : elements) {
				element.divide(scalar);
			}
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
	public final S add(Matrix<?, ?> other) {
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
	public final S subtract(Matrix<?, ?> other) {
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
	public final S multiply(Matrix<?, ?> other) {
		setData2(multiplyData(other.getData2()));

		return getThis();
	}

	@Override
	public final S preMultiply(Matrix<?, ?> other) {
		setData2(preMultiplyData(other.getData2()));

		return getThis();
	}

	public final List<List<Value<V>>> multiplyData(
			List<? extends List<? extends Value<?>>> otherData) {
		// TODO implement multiplication! include isResiseable() in parameter
		// dimensions check...

		return null;
	}

	protected final List<List<Value<V>>> preMultiplyData(
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

	protected final S resizeRowsImplementation(int dimensions) {
		if (getOrder() == Order.RowMajor) {
			return resizeMajorImplementation(dimensions);
		} else {
			return resizeMinorImplementation(dimensions);
		}
	}

	protected final S resizeColumnsImplementation(int dimensions) {
		if (getOrder() == Order.ColumnMajor) {
			return resizeMajorImplementation(dimensions);
		} else {
			return resizeMinorImplementation(dimensions);
		}
	}

	protected final S resizeMajorImplementation(int dimensions) {
		try {
			DimensionalityException.checkValid(dimensions);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		for (List<V> elements : data) {
			while (dimensions < elements.size()) {
				getDependencies().remove(elements.remove(elements.size() - 1));
			}
			while (dimensions > elements.size()) {
				V element = elements.get(0).copy().setValue(0);
				elements.add(element);
				getDependencies().add(element);
			}
		}

		return getThis();
	}

	protected final S resizeMinorImplementation(int dimensions) {
		try {
			DimensionalityException.checkValid(dimensions);
		} catch (DimensionalityException e) {
			throw new IllegalArgumentException(e);
		}

		while (dimensions < data.size()) {
			getDependencies().removeAll(data.remove(data.size() - 1));
		}
		while (dimensions > data.size()) {
			List<V> elements = new ArrayList<V>();
			for (V element : data.get(0)) {
				elements.add(element.copy().setValue(0));
			}
			data.add(elements);
			getDependencies().addAll(elements);
		}

		return getThis();
	}

	protected final S resizeImplementation(Vector<?, IntValue> dimensions) {
		resizeImplementation(dimensions.getData().get(0).intValue(), dimensions
				.getData().get(1).intValue());

		return getThis();
	}

	protected final S resizeImplementation(int rows, int columns) {
		resizeRowsImplementation(rows);
		resizeColumnsImplementation(columns);

		return getThis();
	}

	@Override
	public final int getDataSize() {
		return getMajorSize() * getMinorSize();
	}

	@Override
	public final int getMajorSize() {
		return data.get(0).size();
	}

	@Override
	public final int getMinorSize() {
		return data.size();
	};

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
	public int getRowSize() {
		return getOrder() == Order.RowMajor ? getMajorSize() : getMinorSize();
	}

	@Override
	public int getColumnSize() {
		return getOrder() == Order.ColumnMajor ? getMajorSize() : getMinorSize();
	}

	@Override
	public boolean isSquare() {
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

	@Override
	public List<? extends Vector<?, V>> getRowVectors() {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	@Override
	public List<? extends Vector<?, V>> getColumnVectors() {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVectors();
		} else {
			return getMinorVectors();
		}
	}

	@Override
	public Vector<?, V> getRowVector(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVector(row);
		} else {
			return getMinorVector(row);
		}
	}

	protected List<V> getRowVectorData(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectorData(row);
		} else {
			return getMinorVectorData(row);
		}
	}

	@Override
	public Vector<?, V> getColumnVector(int column) {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVector(column);
		} else {
			return getMinorVector(column);
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
	public List<? extends Vector<?, V>> getMajorVectors() {
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

	@Override
	public List<? extends Vector<?, V>> getMinorVectors() {
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
		return data.get(major).get(minor);
	}

	@Override
	public final S set(Matrix<?, ?> to) {
		setOrder(to.getOrder(), false);

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

		getDependencies().clear();

		for (List<V> major : data) {
			for (V element : major) {
				element = operator.apply(element);
			}
		}

		getDependencies().addAll(getData());

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
			return operateOnData2(order, to, new AssignmentOperation<V, V>() {
				@Override
				public void assign(V firstOperand, V secondOperand) {
					firstOperand.setValue(secondOperand);
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
			return operateOnData(order, to, new AssignmentOperation<V, V>() {
				@Override
				public void assign(V firstOperand, V secondOperand) {
					if (secondOperand == null) {
						throw new IllegalArgumentException(new NullPointerException());
					}

					firstOperand.setValue(secondOperand);
				}
			});
		}
	}

	@Override
	public final S setData(boolean setByReference, List<? extends V> to) {
		return setData(setByReference, getOrder(), to);
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
	public final S setData2(Order order, int[]... data) {
		return setData2(Arrays
				.stream(data)
				.map(data2 -> new IntArrayListView<>(data2, IntValueFactory.instance()))
				.collect(Collectors.toList()));
	}

	@Override
	public final S setData2(Order order, long[]... to) {
		return setData2(Arrays
				.stream(to)
				.map(
						d -> Arrays.stream(d).mapToObj(e -> new LongValue(e))
								.collect(Collectors.toList())).collect(Collectors.toList()));
	}

	@Override
	public final S setData2(Order order, float[]... to) {
		return setData2(order,
				new ListTransformationView<float[], List<FloatValue>>(
						Arrays.asList(to), input -> new FloatArrayListView<>(input,
								FloatValueFactory.instance())));
	}

	@Override
	public final S setData2(Order order, double[]... to) {
		return setData2(
				order,
				new ListTransformationView<double[], List<DoubleValue>>(Arrays
						.asList(to), input -> new DoubleArrayListView<>(input,
						DoubleValueFactory.instance())));
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
}
