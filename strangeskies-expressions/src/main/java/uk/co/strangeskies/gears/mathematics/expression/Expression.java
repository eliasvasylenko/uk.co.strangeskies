package uk.co.strangeskies.gears.mathematics.expression;

import uk.co.strangeskies.gears.utilities.Observable;
import uk.co.strangeskies.gears.utilities.Observer;

/**
 * An expression for use in reactive programming. <br />
 * <br />
 *
 * This class is intended to be {@link Observable} over a specific behaviour:
 * its {@link Observer}s should be notified any time the expression changes.
 * More precisely, they should be notified at any moment at which the value
 * which would be returned from a call getValue() is different from the value
 * which would have been returned before.
 *
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the result value of this expression.
 */
public interface Expression<T> extends Observable<Expression<T>> {
	/**
	 * This should always return the correct value for this Expression. Be careful
	 * to remember that the object returned from getValue() should not be an
	 * updatable reference, i.e. it should be either an immutable class or a const
	 * reference. This is important, but it does not mean that the return value
	 * can necessarily be relied upon not to change when this expression is
	 * updated. <br />
	 * <br />
	 *
	 * Once a value has been returned, it is up to the implementing Expression as
	 * to whether the value will be reliable such that it will remain the same
	 * even if the conceptual value of this expression subsequently changes, or
	 * whether it will update automatically with the expression. Please only rely
	 * on either behaviour if it is explicitly documented, otherwise copy the
	 * returned value if you need a persistent reference which is safe to update
	 * and safe from updates, or re-fetch the result when you need the updated
	 * value. <br />
	 * <br />
	 *
	 * Neither behaviour is explicitly required in preference to the other as much
	 * of the time it could be very wasteful to require a value be copied, but at
	 * the same time, it could often be impossible to return a persistently linked
	 * value, e.g. for the case of a value type which is an immutable class, such
	 * as String.
	 *
	 * @return The fully evaluated value of this Expression at the time of method
	 *         invocation.
	 */
	public T getValue();

	public default T decoupleValue() {
		return getValue();
	}

	public static <T> Expression<T> immutable(final T value) {
		return new Expression<T>() {
			@Override
			public final void clearObservers() {
			}

			@Override
			public final T getValue() {
				return value;
			}

			@Override
			public final boolean addObserver(Observer<? super Expression<T>> observer) {
				return true;
			}

			@Override
			public final boolean removeObserver(
					Observer<? super Expression<T>> observer) {
				return true;
			}
		};
	}
}
