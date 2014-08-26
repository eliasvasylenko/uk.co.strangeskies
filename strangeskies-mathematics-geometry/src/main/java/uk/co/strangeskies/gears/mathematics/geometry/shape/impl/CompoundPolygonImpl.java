package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.CompoundPolygon;
import uk.co.strangeskies.gears.mathematics.geometry.shape.Polygon;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.mathematics.values.ValueFactory;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 * 
 * @author eli
 * 
 * @param <V>
 */
public class CompoundPolygonImpl<V extends Value<V>> extends
		PolygonImpl<CompoundPolygon<V>, V> implements CompoundPolygon<V> {
	public CompoundPolygonImpl(Polygon<?, ?> polygon,
			ValueFactory<? extends V> valueFactory) {
	}

	public CompoundPolygonImpl(Polygon<?, ? extends V> polygon) {
	}

	public CompoundPolygonImpl(List<? extends Vector2<?>> polygon,
			ValueFactory<? extends V> valueFactory) {
	}

	public CompoundPolygonImpl(List<? extends Vector2<V>> polygon) {
	}

	@Override
	public Value<?> getArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Vector2<V>> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompoundPolygon<V> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompoundPolygon<V> set(CompoundPolygon<V> to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Polygon<?, V>> getComponentSet() {
		// TODO Auto-generated method stub
		return null;
	}
}