package uk.co.strangeskies.utilities.collection;

import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtilities {
	private StreamUtilities() {}

	public static <T> Stream<T> flatMapRecursive(Stream<T> stream, Function<? super T, ? extends T> mapping) {
		return new Stream<T>(){};
	}
}
