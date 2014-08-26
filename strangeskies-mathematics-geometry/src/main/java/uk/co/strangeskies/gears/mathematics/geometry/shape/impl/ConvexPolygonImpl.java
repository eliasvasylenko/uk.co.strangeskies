package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.expression.collection.ExpressionSet;
import uk.co.strangeskies.gears.mathematics.expression.collection.ExpressionTreeSet;
import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.ConvexPolygon;
import uk.co.strangeskies.gears.mathematics.values.Value;
import uk.co.strangeskies.gears.utilities.Observable;
import uk.co.strangeskies.gears.utilities.Observer;

//convex only polygon
public class ConvexPolygonImpl<V extends Value<V>> extends
		SimplePolygonImpl<ConvexPolygon<V>, V> implements ConvexPolygon<V> {
	public class ConvexHull<T> extends AbstractSet<Vector2<V>> {
		private final ArrayList<Vector2<V>> backingList;

		private ConvexHull(Observable<?> hullVertexSet) {
			backingList = new ArrayList<>();
			hullVertexSet.addObserver(new Observer<Object>() {
				@Override
				public void notify(Object message) {
					invalidate();
				}
			});
		}

		public void invalidate() {

		}

		private boolean tryAdd(Vector2<V> point) {
			if (backingList.contains(point)) {
				return false;
			}

			List<Vector2<V>> vertices = getVertices();

			// fail if point is already part of shape
			if (touches(point))
				return false;

			// special case for current size < 3
			if (vertices.size() < 3) {
				vertices.add(point);

				// make sure wound in correct direction
				if (vertices.size() == 3
						&& ConvexPolygonImpl.super.getWindingDirection() != getWindingDirection()) {
					vertices.add(vertices.remove(0));
				}
				return true;
			}

			// find the end points of the edge sequence which has the point on the
			// right.

			// was the point on the right of the last edge
			boolean right = false;

			// for (int i = begin; i < end; i++) {
			// // is the point on the right of the edge from this vertex to the next
			// if (new Matrix2Impl<V>(i - (i + 1), i - point).Det() > 0.01) {
			// // was it on the other side last time
			// if (right == false) {
			// right = true;
			// first = i;
			// if (last >= begin)
			// i = end;
			// }
			// } else {
			// // was it on the other side last time
			// if (right == true) {
			// right = false;
			// last = i;
			// if (first > begin)
			// i = end;
			// }
			// }
			// }
			// if (last == begin - 1)
			// last = begin;
			//
			// // construct new convex polygon
			// List<Vector2Impl<V>> newVertices;
			//
			// if (first < last) {
			// for (i = first; i <= last; i++)
			// newVertices.push_back(i);
			// newVertices.push_back(point);
			// } else {
			// for (i = first; i < end; i++)
			// newVertices.push_back(i);
			// for (i = begin; i <= last; i++)
			// newVertices.push_back(i);
			// newVertices.push_back(point);
			// }
			//
			// vertices = newVertices;

			return true;
		}

		@Override
		public Iterator<Vector2<V>> iterator() {
			return backingList.iterator();
		}

		@Override
		public int size() {
			return backingList.size();
		}
	}

	private final ExpressionSet<?, Vector2<V>> vertexSet;
	private final ConvexHull<V> convexHull;

	private WindingDirection windingDirection;

	public ConvexPolygonImpl() {
		vertexSet = new ExpressionTreeSet<Vector2<V>>();
		convexHull = new ConvexHull<V>(vertexSet);
		getDependencies().add(vertexSet);

		windingDirection = WindingDirection.Clockwise;
	}

	public void setWindingDirection(WindingDirection windingDirection) {
		this.windingDirection = windingDirection;

		update();
	}

	@Override
	public WindingDirection getWindingDirection() {
		return windingDirection;
	}

	@Override
	public boolean contains(Vector2<?> point) {
		// TODO test on right of left side and on left or right side.
		return false;
	}

	@Override
	public boolean touches(Vector2<?> point) {
		// TODO test on right of left side and on left or right side.
		return false;
	}

	@Override
	public List<Vector2<V>> getVertices() {
		return new ArrayList<>(hullVertexSet());
	}

	@Override
	public ConvexPolygon<V> copy() {
		ConvexPolygon<V> copy = new ConvexPolygonImpl<>();

		Set<Vector2<V>> copyVertexSet = copy.vertexSet();
		for (Vector2<V> vertex : vertexSet()) {
			copyVertexSet.add(vertex.copy());
		}

		return copy;
	}

	@Override
	public ConvexPolygon<V> set(ConvexPolygon<V> to) {
		vertexSet().clear();
		vertexSet().addAll(to.vertexSet());
		return this;
	}

	@Override
	public Set<Vector2<V>> hullVertexSet() {
		return convexHull;
	}

	@Override
	public Set<Vector2<V>> vertexSet() {
		return vertexSet;
	}
}