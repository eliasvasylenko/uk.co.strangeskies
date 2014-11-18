package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public abstract class VectorHImpl<S extends VectorH<S, V>, V extends Value<V>>
		extends /* @ReadOnly */VectorImpl<S, V> implements VectorH<S, V> {
	private Type type;

	public VectorHImpl(Type type, int size, Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(size + 1, order, orientation, valueFactory);

		getElement(size).setValue(type == Type.Relative ? 0 : 1);

		this.type = type;
	}

	public VectorHImpl(Type type, Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, resizeColumnImplementation(type, values));

		this.type = type;
	}

	protected static <V extends Value<V>> List<V> resizeColumnImplementation(
			Type type, List<? extends V> data) {
		List<V> newData = new ArrayList<>(data);

		newData.add(data.get(0).copy().setValue(type == Type.Relative ? 0 : 1));

		return newData;
	}

	@Override
	public final Type getType() {
		return type;
	}

	@Override
	public final void setType(Type type) {
		this.type = type;
	}

	@Override
	public final int getProjectedDimensions() {
		return super.getDimensions() - 1;
	}

	@Override
	public final V getElement(int major, int minor) {
		return super.getElement(major, minor);
	}

	@Override
	public final S add(Matrix<?, ?> other) {
		if (!other.getData().get(getProjectedDimensions()).equals(0)) {
			throw new IllegalArgumentException();
		}

		return super.add(other);
	}

	@Override
	public final S subtract(Matrix<?, ?> other) {
		if (!other.getData().get(getProjectedDimensions()).equals(0)) {
			throw new IllegalArgumentException();
		}

		return super.subtract(other);
	}

	@Override
	public final S multiply(double scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(float scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(long scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(int scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(Value<?> scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S divide(double scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(float scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public S divide(long scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(int scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(Value<?> scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S multiply(Matrix<?, ?> other) {
		List<List<Value<V>>> multiplied = Matrix.multiplyData(getData2(),
				other.getData2());

		List<Value<V>> lastElements = multiplied.get(multiplied.size() - 1);
		Value<V> lastElement = lastElements.get(lastElements.size() - 1);
		if (!((lastElement.equals(0) && getType() == Type.Relative) || (lastElement
				.equals(1) && getType() == Type.Absolute)))
			throw new IllegalArgumentException();

		return setData2(multiplied);
	}

	@Override
	public final S preMultiply(Matrix<?, ?> other) {
		List<List<Value<V>>> multiplied = Matrix.preMultiplyData(getData2(),
				other.getData2());

		List<Value<V>> lastElements = multiplied.get(multiplied.size() - 1);
		Value<V> lastElement = lastElements.get(lastElements.size() - 1);
		if (!((lastElement.equals(0) && getType() == Type.Relative) || (lastElement
				.equals(1) && getType() == Type.Absolute)))
			throw new IllegalArgumentException();

		return setData2(multiplied);
	}

	@Override
	public final S translate(Vector<?, ?> translation) {
		if (translation.getDimensions() == getProjectedDimensions()) {
			getMutableVector().translate(translation);
		} else if (translation.getDimensions() == getDimensions()) {
			add(translation);
		}
		return getThis();
	}
}
