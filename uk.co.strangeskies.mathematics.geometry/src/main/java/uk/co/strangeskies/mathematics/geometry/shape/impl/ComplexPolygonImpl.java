package uk.co.strangeskies.mathematics.geometry.shape.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ComplexPolygon;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

public abstract class ComplexPolygonImpl<S extends ComplexPolygon<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements ComplexPolygon<S, V>,
		CopyDecouplingExpression<S> {
	@Override
	public Value<?> getPerimeter() {
		List<Vector2<V>> vertices = boundary().vertices();

		double perimeter = 0;

		Vector2<V> previousVertex = null;
		for (Vector2<V> vertex : vertices) {
			if (previousVertex != null)
				perimeter += previousVertex.getSubtracted(vertex).getSize()
						.doubleValue();

			previousVertex = vertex;
		}

		return new DoubleValue(perimeter);
	}

	@Override
	public final S get() {
		return getThis();
	}

	@Override
	protected final S evaluate() {
		return getThis();
	}

	@Override
	public boolean intersects(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}
}
