package uk.co.strangeskies.utilities.collection;

import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.BaseStreamDecorator;

/**
 * A stream decorator over some sort of resource which only opens the resource
 * upon invocation of a terminal operation, and then guarantees resource closure
 * when the operation is complete.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the elements of the stream
 * @param <S>
 *          the type of the stream
 */
public abstract class ResourceStream<T, S extends BaseStream<T, S>> implements BaseStreamDecorator<T, S> {
	protected static class ReferenceResourceStream<T> extends ResourceStream<T, Stream<T>> implements StreamDecorator<T> {
		private ReferenceResourceStream(Stream<T> component, Runnable openResource, Runnable closeResource) {
			super(component, openResource, closeResource);
		}

		@Override
		public Iterator<T> iterator() {
			return collect(toList()).iterator();
		}

		@Override
		public Spliterator<T> spliterator() {
			return collect(toList()).spliterator();
		}
	}

	protected static class IntResourceStream extends ResourceStream<Integer, IntStream> implements IntStreamDecorator {
		private IntResourceStream(IntStream component, Runnable openResource, Runnable closeResource) {
			super(component, openResource, closeResource);
		}

		@Override
		public PrimitiveIterator.OfInt iterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToInt(i -> i).iterator();
		}

		@Override
		public Spliterator.OfInt spliterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToInt(i -> i).spliterator();
		}
	}

	protected static class LongResourceStream extends ResourceStream<Long, LongStream> implements LongStreamDecorator {
		private LongResourceStream(LongStream component, Runnable openResource, Runnable closeResource) {
			super(component, openResource, closeResource);
		}

		@Override
		public PrimitiveIterator.OfLong iterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToLong(i -> i).iterator();
		}

		@Override
		public Spliterator.OfLong spliterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToLong(i -> i).spliterator();
		}
	}

	protected static class DoubleResourceStream extends ResourceStream<Double, DoubleStream>
			implements DoubleStreamDecorator {
		private DoubleResourceStream(DoubleStream component, Runnable openResource, Runnable closeResource) {
			super(component, openResource, closeResource);
		}

		@Override
		public PrimitiveIterator.OfDouble iterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToDouble(i -> i).iterator();
		}

		@Override
		public Spliterator.OfDouble spliterator() {
			// this is pretty lazy, we can definitely do better
			return boxed().collect(toList()).stream().mapToDouble(i -> i).spliterator();
		}
	}

	/**
	 * @param <U>
	 *          the type of the elements of the stream
	 * @param stream
	 *          a reference stream over the given resource strategy
	 * @param openResource
	 *          the strategy to open the resource
	 * @param closeResource
	 *          the strategy to close the resource
	 * @return a suitable implementation of {@link ResourceStream} over the given
	 *         stream
	 */
	public static <U> StreamDecorator<U> over(Stream<U> stream, Runnable openResource, Runnable closeResource) {
		return new ReferenceResourceStream<>(stream, openResource, closeResource);
	}

	/**
	 * @param stream
	 *          an integer stream over the given resource strategy
	 * @param openResource
	 *          the strategy to open the resource
	 * @param closeResource
	 *          the strategy to close the resource
	 * @return a suitable implementation of {@link ResourceStream} over the given
	 *         stream
	 */
	public static IntStream over(IntStream stream, Runnable openResource, Runnable closeResource) {
		return new IntResourceStream(stream, openResource, closeResource);
	}

	/**
	 * @param stream
	 *          a long stream over the given resource strategy
	 * @param openResource
	 *          the strategy to open the resource
	 * @param closeResource
	 *          the strategy to close the resource
	 * @return a suitable implementation of {@link ResourceStream} over the given
	 *         stream
	 */
	public static LongStream over(LongStream stream, Runnable openResource, Runnable closeResource) {
		return new LongResourceStream(stream, openResource, closeResource);
	}

	/**
	 * @param stream
	 *          a double stream over the given resource strategy
	 * @param openResource
	 *          the strategy to open the resource
	 * @param closeResource
	 *          the strategy to close the resource
	 * @return a suitable implementation of {@link ResourceStream} over the given
	 *         stream
	 */
	public static DoubleStream over(DoubleStream stream, Runnable openResource, Runnable closeResource) {
		return new DoubleResourceStream(stream, openResource, closeResource);
	}

	private final S component;
	private final Runnable openResource;
	private final Runnable closeResource;

	protected ResourceStream(S component, Runnable openResource, Runnable closeResource) {
		this.component = component;
		this.openResource = openResource;
		this.closeResource = closeResource;
	}

	@Override
	public S component() {
		return component;
	}

	@Override
	public <U> StreamDecorator<U> decorateIntermediateReference(Function<? super S, Stream<U>> transformation) {
		return new ReferenceResourceStream<>(transformation.apply(component()), openResource, closeResource);
	}

	@Override
	public IntStream decorateIntermediateInt(Function<? super S, IntStream> transformation) {
		return new IntResourceStream(transformation.apply(component()), openResource, closeResource);
	}

	@Override
	public LongStream decorateIntermediateLong(Function<? super S, LongStream> transformation) {
		return new LongResourceStream(transformation.apply(component()), openResource, closeResource);
	}

	@Override
	public DoubleStream decorateIntermediateDouble(Function<? super S, DoubleStream> transformation) {
		return new DoubleResourceStream(transformation.apply(component()), openResource, closeResource);
	}

	@Override
	public <U> U decorateTerminal(Function<? super S, ? extends U> transformation) {
		try {
			openResource.run();
			return BaseStreamDecorator.super.decorateTerminal(transformation);
		} finally {
			closeResource.run();
		}
	}

	@Override
	public void decorateVoidTerminal(Consumer<? super S> transformation) {
		try {
			openResource.run();
			BaseStreamDecorator.super.decorateVoidTerminal(transformation);
		} finally {
			closeResource.run();
		}
	}
}
