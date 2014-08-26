package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.gears.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.gears.mathematics.geometry.Bounds2;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.CompoundPolygon;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Polygon;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.Value;

public abstract class PolygonImpl<S extends Polygon<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Polygon<S, V>,
		CopyDecouplingExpression<S> {
	@Override
	public Value<?> getPerimeter() {
		List<Vector2<V>> vertices = getVertices();

		double perimeter = 0;

		Vector2<V> previousVertex = vertices.get(vertices.size() - 1);
		for (Vector2<V> vertex : vertices) {
			perimeter += previousVertex.getSubtracted(vertex).getSize().doubleValue();

			previousVertex = vertex;
		}

		return new DoubleValue(perimeter);
	}

	@Override
	public Bounds2<?> getBounds() {
		return new Bounds2<V>(getVertices());
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
	public boolean contains(Vector2<?> point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Vector2<?> point) {
		// TODO Auto-generated method stub
		return false;
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

	@Override
	public CompoundPolygon<V> and(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getAnd(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).and(value);
	}

	@Override
	public CompoundPolygon<V> nand(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getNand(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).nand(value);
	}

	@Override
	public CompoundPolygon<V> nor(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getNor(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).nor(value);
	}

	@Override
	public CompoundPolygon<V> or(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getOr(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).or(value);
	}

	@Override
	public CompoundPolygon<V> xnor(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getXnor(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).xnor(value);
	}

	@Override
	public CompoundPolygon<V> xor(Polygon<?, V> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompoundPolygon<V> getXor(Polygon<?, V> value) {
		return new CompoundPolygonImpl<V>(getVertices()).xor(value);
	}
}
