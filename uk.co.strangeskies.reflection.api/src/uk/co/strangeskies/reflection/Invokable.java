package uk.co.strangeskies.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Invokable<T, R> implements GenericTypeContext<Executable> {
	private final TypeLiteral<T> receiverType;
	private final TypeLiteral<R> returnType;
	private final Executable executable;

	Invokable(TypeLiteral<T> receiverType, TypeLiteral<R> returnType,
			Executable executable) {
		this.receiverType = receiverType;
		this.returnType = returnType;
		this.executable = executable;
	}

	public static <T> Invokable<T, T> of(Constructor<T> constructor) {
		TypeLiteral<T> type = new TypeLiteral<>(constructor.getDeclaringClass());
		return new Invokable<>(type, type, constructor);
	}

	public static Invokable<?, ?> of(Method method) {
		TypeLiteral<?> type = new TypeLiteral<>(method.getDeclaringClass());
		return new Invokable<>(type, new TypeLiteral<>(Object.class), method);
	}

	public static Invokable<?, ?> of(Executable executable) {
		if (executable instanceof Method)
			return of((Method) executable);
		else
			return of((Constructor<?>) executable);
	}

	@Override
	public Executable getGenericDeclaration() {
		return executable;
	}

	@Override
	public Type getDeclaringType() {
		return receiverType.getType();
	}

	public TypeLiteral<T> getReceiverType() {
		return receiverType;
	}

	public TypeLiteral<R> getReturnType() {
		return returnType;
	}

	public <U extends T> Invokable<U, ? extends R> withReceiverType(
			TypeLiteral<U> type) {
		// TODO also modify return type based on override/more specific constructor.
		return null;
	}

	public <U extends R> Invokable<T, U> withInferredTypes(
			TypeLiteral<U> returnType, TypeLiteral<?>... parameters) {
		return null;
	}

	/*
	 * Infer types when we are already partially parameterized from applicability
	 * verification, or assuming all parameters are passed as null.
	 */
	public <U extends R> Invokable<T, U> withInferredTypes(
			TypeLiteral<U> targetType) {
		return null;
	}

	/*
	 * If the given parameters are not compatible with this invokable in a strict
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withStrictApplicability(Type... parameters) {
		return null;
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withLooseApplicability(Type... parameters) {
		return null;
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, and with variable arity, we return null.
	 * Otherwise, we infer a partial parameterisation where necessary and return
	 * the resulting invokable.
	 */
	public Invokable<T, R> withVariableArityApplicability(Type... parameters) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream()
				.map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	public List<Type> getTypeArguments() {
		return null;
	}

	public Invokable<T, ? extends R> withTypeArgument(
			TypeVariable<? extends Executable> variable, Type instantiation) {
		Arrays.stream(executable.getTypeParameters()).anyMatch(variable::equals);
		return null;
	}

	public Invokable<T, ? extends R> withParameterization(Type... typeArguments) {
		return withParameterization(Arrays.asList(typeArguments));
	}

	public Invokable<T, ? extends R> withParameterization(List<Type> typeArguments) {
		return null;
	}

	public R invoke(T receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	public R invoke(T receiver, List<? extends Object> arguments) {
		return null;
	}

	public R invokeSafely(T receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	public R invokeSafely(T receiver, List<? extends TypedObject<?>> arguments) {
		return null;
	}
}
