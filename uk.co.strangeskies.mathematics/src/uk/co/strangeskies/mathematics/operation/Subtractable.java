package uk.co.strangeskies.mathematics.operation;

import org.checkerframework.checker.igj.qual.Mutable;
import org.checkerframework.checker.igj.qual.ReadOnly;

import uk.co.strangeskies.utilities.Self;

public interface Subtractable<S extends Subtractable<S, T>, T> extends
		Addable<S, T>, Self<S> {
	/**
	 * subtract the value from this
	 *
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public S subtract(@Mutable Subtractable<S, T> this, @ReadOnly T value);

	/**
	 * subtract the value from a copy of this
	 *
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public default S getSubtracted(@ReadOnly Subtractable<S, T> this,
			@ReadOnly T value) {
		return copy().subtract(value);
	}
}