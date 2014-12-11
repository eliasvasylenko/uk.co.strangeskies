package uk.co.strangeskies.reflection;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public interface InvocationResolver<T> {
	public List<Type> inferTypes(Executable executable, Type result,
			Type... parameters);

	public Method resolveOverload(String methodName, Type result,
			Type... parameters);

	public boolean validateParameterization(Executable executable,
			Type... typeArguments);

	public Object invokeWithParameterization(Executable executable,
			Type[] typeArguments, T receiver, Object... parameters);

	public Object invokeSafely(Executable executable, T receiver,
			Object... parameters);
}
