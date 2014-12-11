package uk.co.strangeskies.reflection.proxy;

import java.util.function.Function;

import uk.co.strangeskies.reflection.TypeLiteral;

public interface PartialOverrideProxyFactory {
	public <T> Function<T, T> create(Class<T> baseClass,
			Class<? extends T> partialImplementation);

	public <T> Function<T, T> create(Class<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory);

	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Class<? extends T> partialImplementation);

	public <T> Function<T, T> create(TypeLiteral<T> baseClass,
			Function<T, Class<? extends T>> partialImplementationFactory);
}
