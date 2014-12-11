package uk.co.strangeskies.reflection;

import java.lang.reflect.Type;

public interface InvocationResolverFactory {
	public <T> InvocationResolver<T> over(TypeLiteral<T> receiverType);

	public <T> InvocationResolver<T> over(Class<T> receiverType);

	public InvocationResolver<?> over(Type receiverType);
}
