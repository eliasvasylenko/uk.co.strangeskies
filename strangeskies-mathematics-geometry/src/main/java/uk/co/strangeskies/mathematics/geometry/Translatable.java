package uk.co.strangeskies.mathematics.geometry;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.utilities.Self;

public interface Translatable<S extends Translatable<S>> extends Self<S> {
	public S translate(Vector<?, ?> translation);

	public default S getTranslated(Vector<?, ?> translation) {
		return copy().translate(translation);
	}
}
