package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.SimplePolygon;
import uk.co.strangeskies.gears.mathematics.values.DoubleValue;
import uk.co.strangeskies.gears.mathematics.values.Value;

//simple and potentially self touching polygons (relatively simple)
public abstract class SimplePolygonImpl<S extends SimplePolygon<S, V>, V extends Value<V>>
		extends PolygonImpl<S, V> implements SimplePolygon<S, V> {
	@Override
	public WindingDirection getWindingDirection() {
		return getSignedArea() > 0 ? WindingDirection.Clockwise
				: WindingDirection.Anticlockwise;
	}

	@Override
	public Value<?> getArea() {
		return new DoubleValue(getSignedArea()).modulus();
	}

	private double getSignedArea() {
		List<Vector2<V>> vertices = getVertices();

		double area = 0;

		Vector2<V> previousVertex = vertices.get(vertices.size() - 1);
		for (Vector2<V> vertex : vertices) {
			area += (vertex.getY().doubleValue() - previousVertex.getY()
					.doubleValue())
					* (vertex.getX().doubleValue() + vertex.getX().doubleValue()) * 0.5;

			previousVertex = vertex;
		}

		return area;
	}
}
