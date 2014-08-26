package uk.co.strangeskies.gears.mathematics.geometry.matrix.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.MatrixS;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public abstract class MatrixSImpl<S extends MatrixS<S, V>, V extends Value<V>>
		extends MatrixImpl<S, V> implements MatrixS<S, V> {
	public MatrixSImpl(int size, Order order, Factory<V> valueFactory) {
		super(size, size, order, valueFactory);
	}

	public MatrixSImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		assertIsSquare(this);
	}

	@Override
	public V getDeterminant() {
		return getDeterminant(this);
	}

	public static <V extends Value<V>> V getDeterminant(MatrixS<?, V> matrixSImpl) {
		if (!matrixSImpl.isSquare())
			throw new IllegalArgumentException();

		// TODO implement...
		return null;
	}

	@Override
	public final S transpose() {
		return transposeImplementation();
	}

	@Override
	public int getDimensions() {
		return getMinorSize();
	}

	@Override
	public boolean isSquare() {
		return true;
	}

	protected S resize(int size) {
		return resizeImplementation(size, size);
	}
}
