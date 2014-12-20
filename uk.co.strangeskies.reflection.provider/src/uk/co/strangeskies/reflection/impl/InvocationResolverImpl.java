package uk.co.strangeskies.reflection.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.InvocationResolver;
import uk.co.strangeskies.reflection.TypeLiteral;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class InvocationResolverImpl<T> implements InvocationResolver<T> {
	private final Type receiverType;

	private InvocationResolverImpl(Type receiverType) {
		this.receiverType = receiverType;
	}

	public static <T> InvocationResolverImpl<T> over(TypeLiteral<T> receiverType) {
		return new InvocationResolverImpl<>(receiverType.getType());
	}

	public static <T> InvocationResolverImpl<T> over(Class<T> receiverType) {
		if (receiverType.getTypeParameters().length > 0)
			throw new IllegalArgumentException(
					"Cannot resolve invocations over raw type '" + receiverType + "'.");
		return new InvocationResolverImpl<>(receiverType);
	}

	public static InvocationResolverImpl<?> over(Type receiverType) {
		boolean fullyResolved = true; // TODO verify we reference no TypeVariables
		if (!fullyResolved)
			throw new IllegalArgumentException(
					"Cannot resolve invocations over partially resolved type '"
							+ receiverType + "'.");

		return new InvocationResolverImpl<>(receiverType);
	}

	@Override
	public List<Type> inferTypes(Executable executable, Type result,
			Type... parameters) {
		return inferTypes(
				executable instanceof Method ? Invokable.from((Method) executable)
						: Invokable.from((Constructor<?>) executable), result,
				Arrays.asList(parameters));
	}

	public <R> List<TypeToken<?>> inferTypes(
			Invokable<? super T, ? super R> invokable, TypeToken<R> result,
			TypeToken<?>... parameters) {
		return inferTypes(
				invokable,
				result.getType(),
				Arrays.asList(parameters).stream().map(TypeToken::getType)
						.collect(Collectors.toList())).stream().map(TypeToken::of)
				.collect(Collectors.toList());
	}

	private List<Type> inferTypes(Invokable<?, ?> invokable, Type result,
			List<Type> parameters) {
		return null;
	}

	@Override
	public Method resolveOverload(String name, Type result, Type... parameters) {
		return null;
	}

	public <R> Invokable<? super T, ? extends R> resolveOverload(String name,
			TypeToken<R> result, TypeToken<?>... parameters) {
		return null;
	}

	@Override
	public boolean validateParameterization(Executable executable,
			Type... typeArguments) {
		return false;
	}

	@Override
	public Object invokeWithParameterization(Executable executable,
			Type[] typeArguments, T receiver, Object... parameters) {
		return null;
	}

	public <R> R invokeWithParameterization(Invokable<T, R> executable,
			List<TypeToken<?>> typeArguments, T receiver, Object... parameters) {
		return null;
	}

	@Override
	public Object invokeSafely(Executable executable, T receiver,
			Object... parameters) {
		return null;
	}

	public <R> R invokeSafely(Invokable<T, R> invokable, T receiver,
			Object... parameters) {
		return null;
	}
}
