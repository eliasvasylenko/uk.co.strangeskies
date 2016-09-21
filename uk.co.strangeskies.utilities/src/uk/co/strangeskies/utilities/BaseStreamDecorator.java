package uk.co.strangeskies.utilities;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.collection.StreamDecorator;

/**
 * A decorator for a {@link BaseStream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * <p>
 * Intermediate operations will return a wrapper over the result of applying
 * that operation to the component, such that raw references to component
 * streams cannot escape their wrapping.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the elements of the stream
 * @param <S>
 *          the type of the stream
 */
public interface BaseStreamDecorator<T, S extends BaseStream<T, S>> extends BaseStream<T, S> {
	/**
	 * @return the component reference
	 */
	S component();

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	S decorateIntermediate(Function<? super S, S> transformation);

	/**
	 * @param <U>
	 *          the type of the resulting stream elements
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	<U> StreamDecorator<U> decorateIntermediateReference(Function<? super S, Stream<U>> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	IntStream decorateIntermediateInt(Function<? super S, IntStream> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	LongStream decorateIntermediateLong(Function<? super S, LongStream> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	DoubleStream decorateIntermediateDouble(Function<? super S, DoubleStream> transformation);

	/**
	 * @param <U>
	 *          the type of the termination result
	 * @param termination
	 *          the terminal operation as a function on a stream
	 * @return the result of application of the termination to the component
	 *         stream
	 */
	default <U> U decorateTerminal(Function<? super S, ? extends U> termination) {
		return termination.apply(component());
	}

	/**
	 * @param termination
	 *          the terminal operation as a function on a stream
	 */
	default void decorateVoidTerminal(Consumer<? super S> termination) {
		termination.accept(component());
	}

	@Override
	default S sequential() {
		return decorateIntermediate(BaseStream::sequential);
	}

	@Override
	default S parallel() {
		return decorateIntermediate(BaseStream::parallel);
	}

	@Override
	default S unordered() {
		return decorateIntermediate(BaseStream::unordered);
	}

	@Override
	default S onClose(Runnable closeHandler) {
		return decorateIntermediate(s -> s.onClose(closeHandler));
	}

	@Override
	default Iterator<T> iterator() {
		return decorateTerminal(BaseStream::iterator);
	}

	@Override
	default Spliterator<T> spliterator() {
		return decorateTerminal(BaseStream::spliterator);
	}

	@Override
	default boolean isParallel() {
		return component().isParallel();
	}

	@Override
	default void close() {
		component().close();
	}
}
