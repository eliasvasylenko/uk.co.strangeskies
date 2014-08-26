package uk.co.strangeskies.gears.mathematics;

import uk.co.strangeskies.gears.utilities.Self;

public interface Subtractable<S extends Subtractable<S, T>, T> extends
		Addable<S, T>, Self<S> {
	/**
	 * subtract the value from this
	 * 
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public S subtract(/*@ReadOnly*/T value);

	/**
	 * subtract the value from a copy of this
	 * 
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public S getSubtracted(
	/*@ReadOnly Subtractable<S> this,*//*@ReadOnly*/T value);
}
