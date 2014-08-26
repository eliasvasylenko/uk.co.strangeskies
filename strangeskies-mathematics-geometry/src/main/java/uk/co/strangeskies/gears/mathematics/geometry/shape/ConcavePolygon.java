package uk.co.strangeskies.gears.mathematics.geometry.shape;

import uk.co.strangeskies.gears.mathematics.values.Value;

public interface ConcavePolygon<V extends Value<V>> extends
		SimplePolygon<ConcavePolygon<V>, V> {
	public void sanitise(int toleranceLevel);
}
