package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class CaptureType implements Type {
	private final static AtomicLong COUNTER = new AtomicLong();

	private final String name;
	private final Type[] upperBounds;
	private final Type[] lowerBounds;

	protected CaptureType(Type[] upperBounds, Type[] lowerBounds) {
		this("CAP", upperBounds, lowerBounds);
	}

	protected CaptureType(String name, Type[] upperBounds, Type[] lowerBounds) {
		this.name = name + "#" + COUNTER.incrementAndGet();

		this.upperBounds = upperBounds.clone();
		this.lowerBounds = lowerBounds.clone();
	}

	public String getName() {
		return name;
	}

	@Override
	public String getTypeName() {
		return getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	public Type[] getUpperBounds() {
		return upperBounds.clone();
	}

	public Type[] getLowerBounds() {
		return lowerBounds.clone();
	}

	protected static <T extends Type, C extends CaptureType> Map<T, C> capture(
			Collection<? extends T> types, Function<T, C> captureFunction) {
		Map<T, C> captures = types.stream().collect(
				Collectors.toMap(Function.identity(), captureFunction::apply));

		substituteBounds(captures);

		return captures;
	}

	private static <T extends Type> void substituteBounds(
			Map<T, ? extends CaptureType> captures) {
		TypeSubstitution substitution = new TypeSubstitution();
		for (T type : captures.keySet())
			substitution = substitution.where(type, captures.get(type));

		for (T type : captures.keySet()) {
			CaptureType capture = captures.get(type);

			for (int i = 0; i < capture.upperBounds.length; i++)
				capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);

			for (int i = 0; i < capture.lowerBounds.length; i++)
				capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);
		}
	}
}
