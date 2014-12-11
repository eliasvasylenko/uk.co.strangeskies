package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;

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
