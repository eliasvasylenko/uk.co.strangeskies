package uk.co.strangeskies.gears.mathematics.geometry.shape.impl;

import java.util.List;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.gears.mathematics.geometry.shape.ConcavePolygon;
import uk.co.strangeskies.gears.mathematics.values.Value;

public class ConcavePolygonImpl<V extends Value<V>> extends
		SimplePolygonImpl<ConcavePolygon<V>, V> implements ConcavePolygon<V> {
	@Override
	public void sanitise(int toleranceLevel) {
		// if (toleranceLevel < 0) {
		// toleranceLevel = 0;
		// }
		// // NEED TO OPTIMISE WITH LINE-SWEEP ALGORITHMS
		//
		// // calibrate tolerance with largest ulp
		// float largestValue = 0;
		// float xAbs, yAbs;
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end(); ++i) {
		// xAbs = fabs((*i).x);
		// if (xAbs > largestValue)
		// largestValue = xAbs;
		//
		// yAbs = fabs((*i).y);
		// if (yAbs > largestValue)
		// largestValue = yAbs;
		// }
		//
		// float tolerance = Ulp(largestValue) * toleranceLevel;
		// // snap together points within tolerence squares and remove zero length
		// segments
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end() - 1; ++i)
		// for (List<Vector2<V>> j : i + 1; j < vertices.end(); ++j)
		// if (ToleranceEqual((*j).x, (*i).x, tolerance) && ToleranceEqual((*j).y,
		// (*i).y, tolerance))
		// if (j == i + 1)
		// vertices.erase(j--);
		// else {
		// (*j).x = (*i).x;
		// (*j).y = (*i).y;
		//
		// // record all touching points found or generated ***********
		// }
		// if (ToleranceEqual(vertices.front().x, vertices.back().x, tolerance) &&
		// ToleranceEqual(vertices.front().y, vertices.back().y, tolerance))
		// vertices.erase(vertices.begin());
		//
		// // snap lines to all points with which they touch the ULP tolerence
		// squares of.
		// tolerance = Ulp(largestValue) * toleranceLevel;
		//
		// // points found touching line, sorted in order
		// std::multimap<float, List<Vector2<V>>::iterator> pointsAlongLine;
		// // iterate through each edge (last, i)
		// List<Vector2<V>>::iterator last = --vertices.end();
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end(); ++i) {
		// Line line = Line(*last, *i);
		//
		// // iterate through each point to check if on line.
		// pointsAlongLine.clear();
		// for (List<Vector2<V>> j : vertices.begin(); j < vertices.end(); ++j) {
		// if (j != i && j != last) { // && j !touching i or last
		// Box box = Box(Vector2<V>(*j) -= Vector2<V>(tolerance), Vector2<V>(*j) +=
		// Vector2<V>(tolerance));
		//
		// if (box.CrossesLine(line)) {
		// float distanceAlongLine = Matrix2(!line.GetAB().Normalize(), *j -
		// line.GetA()).Det();
		//
		// if (distanceAlongLine > 0 && distanceAlongLine < line.GetLength())
		// pointsAlongLine.insert(std::make_pair(distanceAlongLine, j));
		// }
		// }
		// }
		//
		// // snap line to intersection points found
		// for (std::multimap<float, List<Vector2<V>>::iterator> j :
		// pointsAlongLine.begin(); j != pointsAlongLine.end(); ++j) {
		// vertices.insert(i, (*j).first);
		// // record touching points generated ( return value of insert() <|>
		// (*insert).first )
		// }
		//
		// // make sure to perform procedure iteratively by returning to first new
		// subline (if any)
		// if (pointsAlongLine.size() > 0)
		// i = last;
		//
		// // Proceed to next line...
		// last = i;
		// }
		//
		// // find lines crossing lines and add vertices at crossing point
		// // SHOULD BE ROBUST IF WE CAN GUARENTEE SOME MAXIMUM ULP IMPRECISION IN
		// INTERSECTION POINTS
		//
		// // find first intersection (cross not touch. when determining which for
		// // intersection strips, we can detect and remove empty slithers)
		// // 'turn' at first point of intersection
		// // (possibly remove turned section if length is below a
		// // certain cutoff point, must consider other half also)
		// int lastIntersection = 0;
		// boolean finished = false;
		// do {
		//
		// } while(!finished);
	}

	@Override
	public List<Vector2<V>> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConcavePolygon<V> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConcavePolygon<V> set(ConcavePolygon<V> to) {
		// TODO Auto-generated method stub
		return null;
	}
}
