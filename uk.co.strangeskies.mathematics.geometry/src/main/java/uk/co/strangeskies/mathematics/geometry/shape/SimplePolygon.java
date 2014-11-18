package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

/**
 * A potentially self-touching, but non-self-crossing, and otherwise
 * traditionally 'simple', polygon.
 *
 * @author eli
 *
 * @param <S>
 * @param <V>
 */
public interface SimplePolygon<S extends SimplePolygon<S, V>, V extends Value<V>>
		extends /* @ReadOnly */ComplexPolygon<S, V> {
	public enum WindingDirection {
		CLOCKWISE, COUNTER_CLOCKWISE;
	}

	/**
	 * Returns the direction which the vertices of this polygon are wound in.
	 * 
	 * @return
	 */
	WindingDirection getWindingDirection();

	@Override
	default WindingRule windingRule() {
		return WindingRule.EVEN_ODD;
	}

	@Override
	default Value<?> getArea() {
		return new DoubleValue(getSignedArea(WindingDirection.CLOCKWISE)).modulus();
	}

	default double getSignedArea(WindingDirection windingDirection) {
		List<Vector2<V>> vertices = boundary().vertices();

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
