package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.values.Value;

public interface ShapeConfigurator<V extends Value<V>> {
	Line2<V> line();

	PolygonConfigurator<V> polygon();

	Triangle<V> triangle();
}
