package uk.co.strangeskies.gears.mathematics.geometry.matrix.building.impl;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.building.ValueMatrixBuilder;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.impl.Vector4Impl;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.factory.Factory;

public class ValueMatrixBuilderImpl<V extends Value<V>> implements
		ValueMatrixBuilder<V> {
	private final Factory<V> valueFactory;
	private Order order;
	private Orientation orientation;

	public ValueMatrixBuilderImpl(Factory<V> valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public final ValueMatrixBuilder<V> order(Order order) {
		this.order = order;
		return this;
	}

	@Override
	public final ValueMatrixBuilder<V> orientation(Orientation orientation) {
		this.orientation = orientation;
		return this;
	}

	protected final Factory<V> getValueFactory() {
		return valueFactory;
	}

	@Override
	public final Vector2<V> vector2() {
		return new Vector2Impl<>(order, orientation, getValueFactory());
	}

	@Override
	public final Vector3<V> vector3() {
		return new Vector3Impl<>(order, orientation, getValueFactory());
	}

	@Override
	public final Vector4<V> vector4() {
		return new Vector4Impl<>(order, orientation, getValueFactory());
	}
}
