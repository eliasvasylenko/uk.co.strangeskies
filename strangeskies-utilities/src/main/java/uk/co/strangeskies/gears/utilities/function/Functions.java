package uk.co.strangeskies.gears.utilities.function;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Functions {
	private Functions() {
	}

	public static <T, U, I, J, R> BiFunction<T, U, R> compose(
			Function<? super T, ? extends I> a, Function<? super U, ? extends J> b,
			BiFunction<? super I, ? super J, R> combiner) {
		return (v, w) -> combiner.apply(a.apply(v), b.apply(w));
	}

	public static <T, I, R> BiFunction<T, T, R> compose(
			Function<? super T, ? extends I> a,
			BiFunction<? super I, ? super I, R> combiner) {
		return (v, w) -> combiner.apply(a.apply(v), a.apply(w));
	}

	public static <T, U, I, J> BiPredicate<T, U> compose(
			Function<? super T, ? extends I> a, Function<? super U, ? extends J> b,
			BiPredicate<? super I, ? super J> combiner) {
		return (v, w) -> combiner.test(a.apply(v), b.apply(w));
	}

	public static <T, I> BiPredicate<T, T> compose(
			Function<? super T, ? extends I> a,
			BiPredicate<? super I, ? super I> combiner) {
		return (v, w) -> combiner.test(a.apply(v), a.apply(w));
	}

	public static <T, I, R> Function<T, R> combineResults(
			Function<? super T, ? extends I> a, Function<? super T, ? extends I> b,
			BiFunction<? super I, ? super I, R> combiner) {
		return v -> combiner.apply(a.apply(v), b.apply(v));
	}

	public static <T, I, R> Function<T, R> collectIterableResults(
			Function<? super T, ? extends Iterable<I>> a,
			Function<? super T, ? extends Iterable<I>> b, Collector<I, ?, R> s) {
		Function<Iterable<I>, Stream<I>> stream = i -> StreamSupport.stream(
				i.spliterator(), true);
		return collectStreamResults(a.andThen(stream), b.andThen(stream), s);
	}

	public static <T, I, R> Function<T, R> collectStreamResults(
			Function<? super T, ? extends Stream<I>> a,
			Function<? super T, ? extends Stream<I>> b, Collector<I, ?, R> s) {
		return combineResults(a, b, (c, d) -> {
			return Stream.concat(c, d).collect(s);
		});
	}
}
