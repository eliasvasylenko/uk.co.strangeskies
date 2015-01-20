/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;

public class Invokable<T, R> implements GenericTypeContainer<Executable> {
	private final Resolver resolver;

	private final TypeLiteral<T> receiverType;
	private final TypeLiteral<R> returnType;
	private final Executable executable;

	private final List<Type> parameters;

	private final BiFunction<T, List<?>, R> invocationFunction;

	private Invokable(TypeLiteral<T> receiverType, TypeLiteral<R> returnType,
			Executable executable, BiFunction<T, List<?>, R> invocationFunction) {
		this(receiverType.getResolver(), receiverType, returnType, executable,
				invocationFunction);
	}

	@SuppressWarnings("unchecked")
	private Invokable(Resolver resolver, TypeLiteral<T> receiverType,
			TypeLiteral<R> returnType, Executable executable,
			BiFunction<T, List<?>, R> invocationFunction) {
		this.receiverType = receiverType;
		this.executable = executable;
		this.invocationFunction = invocationFunction;

		this.resolver = resolver;
		resolver.capture(getGenericDeclaration());

		if (receiverType != returnType)
			returnType = (TypeLiteral<R>) TypeLiteral.from(resolver
					.resolveType(returnType.getType()));

		this.returnType = returnType;

		parameters = Arrays.stream(executable.getGenericParameterTypes())
				.map(resolver::resolveType).collect(Collectors.toList());
	}

	public static <T> Invokable<T, T> from(Constructor<T> constructor) {
		TypeLiteral<T> type = new TypeLiteral<>(constructor.getDeclaringClass());
		return from(constructor, type);
	}

	static <T> Invokable<T, T> from(Constructor<T> constructor,
			TypeLiteral<T> receiver) {
		return new Invokable<>(receiver, receiver, constructor,
				(T r, List<?> a) -> {
					try {
						return constructor.newInstance(a);
					} catch (Exception e) {
						throw new TypeInferenceException("Cannot invoke constructor '"
								+ constructor + "' with arguments '" + a + "'.");
					}
				});
	}

	public static Invokable<?, ?> from(Method method) {
		return from(method, TypeLiteral.from(method.getDeclaringClass()),
				TypeLiteral.from(method.getGenericReturnType()));
	}

	@SuppressWarnings("unchecked")
	static <T, R> Invokable<T, R> from(Method method, TypeLiteral<T> receiver,
			TypeLiteral<R> result) {
		return new Invokable<>(receiver, result, method, (T r, List<?> a) -> {
			try {
				return (R) method.invoke(r, a);
			} catch (Exception e) {
				throw new TypeInferenceException("Cannot invoke method '" + method
						+ "' on receiver '" + r + "' with arguments '" + a + "'.");
			}
		});
	}

	public static Invokable<?, ?> from(Executable executable) {
		if (executable instanceof Method)
			return from((Method) executable);
		else
			return from((Constructor<?>) executable);
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	public boolean isStrict() {
		return Modifier.isStrict(executable.getModifiers());
	}

	public boolean isSynchronized() {
		return Modifier.isSynchronized(executable.getModifiers());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (isPrivate())
			builder.append("private ");
		else if (isProtected())
			builder.append("protected ");
		else if (isPublic())
			builder.append("public ");

		if (isNative())
			builder.append("native ");
		if (isStatic())
			builder.append("static ");
		if (isStrict())
			builder.append("strictfp ");
		if (isSynchronized())
			builder.append("synchronized ");

		if (isAbstract())
			builder.append("abstract ");
		else if (isFinal())
			builder.append("final ");

		return builder
				.append(returnType)
				.append(" ")
				.append(receiverType)
				.append(".")
				.append(executable.getName())
				.append("(")
				.append(
						parameters.stream().map(Objects::toString)
								.collect(Collectors.joining(", "))).append(")").toString();
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
		throw new UnsupportedOperationException(); // TODO
	}

	public <S extends R> Invokable<T, S> withTargetType(TypeLiteral<S> type) {
		throw new UnsupportedOperationException(); // TODO
	}

	/*
	 * If no arguments passed, but parameters expected, infer types when we are
	 * already partially parameterized from applicability verification, or
	 * assuming all parameters are passed as null.
	 */
	public <U extends R> Invokable<T, U> withInferredTypes(
			TypeLiteral<U> targetType, TypeLiteral<?>... parameters) {
		throw new UnsupportedOperationException(); // TODO
	}

	/*
	 * If the given parameters are not compatible with this invokable in a strict
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withStrictApplicability(Type... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withStrictApplicability(List<? extends Type> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withLooseApplicability(Type... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withLooseApplicability(List<? extends Type> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, and with variable arity, we return null.
	 * Otherwise, we infer a partial parameterisation where necessary and return
	 * the resulting invokable.
	 */
	public Invokable<T, R> withVariableArityApplicability(Type... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withVariableArityApplicability(
			List<? extends Type> arguments) {
		return withLooseApplicability(true, arguments);
	}

	private Invokable<T, R> withLooseApplicability(boolean variableArity,
			List<? extends Type> arguments) {
		if (variableArity) {
			if (!executable.isVarArgs())
				throw new IllegalArgumentException("Invokable '" + this
						+ "' cannot be invoked in variable arity invocation context.");

			if (parameters.size() > arguments.size())
				return null;
		} else if (parameters.size() != arguments.size())
			return null;

		Resolver resolver = new Resolver(this.resolver);

		Iterator<Type> parameters = this.parameters.iterator();
		Type nextParameter = parameters.next();
		Type parameter = nextParameter;
		for (Type argument : arguments) {
			if (nextParameter != null) {
				parameter = nextParameter;
				if (parameters.hasNext())
					nextParameter = parameters.next();
				else if (variableArity) {
					parameter = Types.getComponentType(parameter);
					nextParameter = null;
				}
			}
			resolver.incorporateConstraint(new ConstraintFormula(
					Kind.LOOSE_COMPATIBILILTY, argument, parameter));
		}

		resolver.infer();

		if (!resolver.validate())
			throw new TypeInferenceException(
					"Cannot resolve generic type parameters for invocation of '" + this
							+ "' with arguments '" + arguments + "'.");

		return new Invokable<>(resolver, receiverType, returnType, executable,
				invocationFunction);
	}

	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream()
				.map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	public List<Type> getTypeArguments() {
		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArgument(
			TypeVariable<? extends Executable> variable, Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters())
				.anyMatch(variable::equals)) {
			Resolver resolver = new Resolver(this.resolver);
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	public Invokable<T, ? extends R> withTypeArguments(List<Type> typeArguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	public R invoke(T receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	public R invoke(T receiver, List<? extends Object> arguments) {
		return invocationFunction.apply(receiver, arguments);
	}

	public R invokeSafely(T receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	public R invokeSafely(T receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getType().isAssignableTo(parameters.get(i)))
				throw new IllegalArgumentException("Argument '" + arguments.get(i)
						+ "' is not assignable to parameter '" + parameters.get(i)
						+ "' at index '" + i + "'.");
		return invoke(receiver, arguments);
	}
}
