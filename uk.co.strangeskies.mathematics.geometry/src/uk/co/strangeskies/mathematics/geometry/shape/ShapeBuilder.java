package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public interface ShapeBuilder {
	<V extends Value<V>> ShapeConfigurator<V> with(Factory<V> valueFactory);
}
