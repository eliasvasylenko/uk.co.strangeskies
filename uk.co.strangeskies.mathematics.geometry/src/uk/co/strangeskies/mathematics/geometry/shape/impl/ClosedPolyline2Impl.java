package uk.co.strangeskies.mathematics.geometry.shape.impl;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.Line2;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Observer;

public class ClosedPolyline2Impl<V extends Value<V>> implements
		ClosedPolyline2<V> {
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
	public Bounds2<V> getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClosedPolyline2<V> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClosedPolyline2<V> set(ClosedPolyline2<V> to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClosedPolyline2<V> get() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClosedPolyline2<V> getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadWriteLock getLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addObserver(
			Observer<? super Expression<ClosedPolyline2<V>>> observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeObserver(
			Observer<? super Expression<ClosedPolyline2<V>>> observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearObservers() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Line2<V>> lines() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Vector2<V>> vertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value<?> getLength() {
		// TODO Auto-generated method stub
		return null;
	}

}
