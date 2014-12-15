package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Type;

import uk.co.strangeskies.reflection.InvocationResolver;
import uk.co.strangeskies.reflection.InvocationResolverFactory;
import uk.co.strangeskies.reflection.TypeLiteral;

public class InvocationResolverFactoryImpl implements InvocationResolverFactory {
	@Override
	public <T> InvocationResolver<T> over(TypeLiteral<T> receiverType) {
		return InvocationResolverImpl.over(receiverType);
	}

	@Override
	public <T> InvocationResolver<T> over(Class<T> receiverType) {
		return InvocationResolverImpl.over(receiverType);
	}

	@Override
	public InvocationResolver<?> over(Type receiverType) {
		return InvocationResolverImpl.over(receiverType);
	}
}
