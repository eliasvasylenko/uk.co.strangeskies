package uk.co.strangeskies.gears.mathematics.geometry;

import uk.co.strangeskies.gears.mathematics.geometry.matrix.vector.Vector;

public interface NonCommutativelyTranslatable<S extends NonCommutativelyTranslatable<S>>
		extends Translatable<S> {
	public S preTranslate(Vector<?, ?> translation);

	public S getPreTranslated(Vector<?, ?> translation);
}
