package uk.co.strangeskies.mathematics.geometry.shape;

import uk.co.strangeskies.mathematics.expression.Variable;
import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Copyable;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;

/**
 * TODO: Decide whether the following decision is sensible! Maybe we should
 * require all shapes be affinely transformable? Would be okay for polygons,
 * ellipses, and beziers. Maybe we should use homogeneous coordinates after all?
 * 
 * Also TODO, think about passing in triangulator to polygon builder and have
 * shapes triangulate internally. This would help in cases where triangulation
 * is necessary for calculation of other properties, so as not to have to
 * recalculate it...
 * 
 * In general, classes and interfaces which extend Shape should deal with
 * <em>non-homogeneous</em> vectors, in the interests of a simple and consistent
 * API. Shapes don't generally support affine transformations or related
 * operations, so there is little value in trying to make homogeneous coordinate
 * space a consistent part of the API at this level. Classes related to
 * {@link Mesh}, on the other hand, do deal with homogeneous coordinate space.
 * 
 * @author eli
 *
 * @param <S>
 */
public interface Shape<S extends Shape<S>> extends Self<S>, Copyable<S>,
		Property<S, S>, Variable<S> {
	Value<?> getArea(/* @readOnly this */);

	Value<?> getPerimeter(/* @readOnly this */);

	boolean contains(
	/* @readOnly this, *//* @ReadOnly */Vector2<?> point);

	boolean touches(/* @readOnly this, *//* @ReadOnly */Vector2<?> point);

	boolean intersects(
	/* @readOnly this, *//* @ReadOnly */Shape<?> shape);

	boolean touches(/* @readOnly this, *//* @ReadOnly */Shape<?> shape);

	Bounds2<?> getBounds(/* @readOnly this */);

	@Override
	default S get() {
		return getThis();
	}
}
