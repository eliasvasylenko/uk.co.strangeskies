package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;

public interface NonCommutativelyTranslatable<S extends NonCommutativelyTranslatable<S>>
		extends Translatable<S> {
	public S preTranslate(Vector<?, ?> translation);

	public default S getPreTranslated(Vector<?, ?> translation) {
		return copy().getPreTranslated(translation);
	}
}
