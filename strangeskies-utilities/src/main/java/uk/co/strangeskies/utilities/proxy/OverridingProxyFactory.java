package uk.co.strangeskies.utilities.proxy;

import java.util.function.Function;

public class OverridingProxyFactory<T> {
	public OverridingProxyFactory(
			Function<T, Class<? extends T>> partialImplementationFactory) {
		
	}
}
