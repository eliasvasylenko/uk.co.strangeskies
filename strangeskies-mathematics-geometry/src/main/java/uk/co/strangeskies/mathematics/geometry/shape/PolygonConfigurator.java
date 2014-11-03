package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.values.Value;

public interface PolygonConfigurator<V extends Value<V>> {
	CompoundPolygon<?, V> compound();

	ComplexPolygon<?, V> complex();

	SimplePolygon<?, V> simple();

	ConvexPolygon<?, V> convex();
}
