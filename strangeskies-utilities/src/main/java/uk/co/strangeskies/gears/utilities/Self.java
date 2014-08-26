package uk.co.strangeskies.gears.utilities;

/**
 * For classes which follow the self-bounding pattern. The self-bounding pattern
 * is the use of the final derived class as a type parameter.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <S>
 *          The final implementing class.
 */
/* @I */
public interface Self<S extends Self<S>> extends Copyable<S> {
	@SuppressWarnings("unchecked")
	public default/* @I */S getThis() {
		return (S) this;
	}
}
