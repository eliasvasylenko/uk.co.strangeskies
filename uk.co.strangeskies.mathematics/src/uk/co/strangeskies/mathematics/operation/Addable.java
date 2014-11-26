package uk.co.strangeskies.mathematics.operation;

import org.checkerframework.checker.igj.qual.Mutable;
import org.checkerframework.checker.igj.qual.ReadOnly;

import uk.co.strangeskies.utilities.Self;

public interface Addable<S extends Addable<S, T>, T> extends Self<S> {
	/**
	 * add the value to this
	 *
	 * @param value
	 *          the value to add to this
	 * @return this
	 */
	public S add(@Mutable Addable<S, T> this, @ReadOnly T value);

	/**
	 * add the value to a copy of this
	 *
	 * @param value
	 *          the value to add to the copy
	 * @return the copy with the added value
	 */
	public default S getAdded(@ReadOnly Addable<S, T> this, @ReadOnly T value) {
		return copy().add(value);
	}
}
