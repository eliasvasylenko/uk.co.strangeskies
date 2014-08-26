package uk.co.strangeskies.gears.utilities.function;

import java.util.function.Function;

/**
 * Describes a function from F to T. A function should be stateless, hence the
 * implementing of {@link Immutable}.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          To
 * @param <F>
 *          From
 */
public interface InvertibleFunction<T, R> extends Function<T, R> {
	public InvertibleFunction<R, T> getInverse();
}
