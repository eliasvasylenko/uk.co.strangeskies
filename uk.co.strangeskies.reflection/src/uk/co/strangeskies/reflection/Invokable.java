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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.tuples.Pair;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes. Instances of this class
 * can be created from instances of Executable directly from
 * {@link #from(Executable)} and its overloads, or using the
 * {@link TypeToken#resolveConstructorOverload(List)} and
 * {@link TypeToken#resolveMethodOverload(String, List)} methods on TypeToken
 * instances.
 * </p>
 * 
 * <p>
 * Invokables may be created over types which mention inference variables, or
 * even over inference variables themselves.
 * </p>
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The receiver type of the executable.
 * @param <R>
 *          The return type of the executable.
 */
public class Invokable<T, R> {
	private final Resolver resolver;

	private final TypeToken<T> receiverType;
	private final TypeToken<R> returnType;
	private final Executable executable;

	private final List<Type> parameters;

	private final BiFunction<T, List<?>, R> invocationFunction;

	protected Invokable(Invokable<T, R> that) {
		resolver = that.resolver;
		receiverType = that.receiverType;
		returnType = that.returnType;
		executable = that.executable;
		parameters = that.parameters;
		invocationFunction = that.invocationFunction;
	}

	private Invokable(TypeToken<T> receiverType, TypeToken<R> returnType,
			Executable executable, BiFunction<T, List<?>, R> invocationFunction) {
		this(receiverType.getResolver(), receiverType, returnType, executable,
				invocationFunction);
	}

	@SuppressWarnings("unchecked")
	private Invokable(Resolver resolver, TypeToken<T> receiverType,
			TypeToken<R> returnType, Executable executable,
			BiFunction<T, List<?>, R> invocationFunction) {
		this.resolver = resolver;

		this.executable = executable;
		resolver.incorporateTypeParameters(getExecutable());

		this.receiverType = (TypeToken<T>) TypeToken.of(resolver,
				receiverType.getType());

		this.returnType = (TypeToken<R>) TypeToken.of(resolver,
				returnType.getType());

		parameters = Arrays.stream(executable.getGenericParameterTypes())
				.map(resolver::resolveType).collect(Collectors.toList());

		this.invocationFunction = invocationFunction;
	}

	/**
	 * Create a new Invokable instance from a reference to a {@link Constructor}.
	 * The type of T here should only ever be raw, and only its raw type will be
	 * available for inspection reflectively, so it is possible to subsequently
	 * specify the receiver type using an overload of
	 * {@link #withReceiverType(TypeToken)}, or to attempt to infer a more
	 * specific type from a target type using an overload of
	 * {@link #withTargetType(TypeToken)}.
	 * 
	 * @param constructor
	 * @return An invokable wrapping the given constructor.
	 */
	public static <T> Invokable<T, T> from(Constructor<T> constructor) {
		TypeToken<T> type = TypeToken.of(constructor.getDeclaringClass());
		return from(constructor, type);
	}

	static <T> Invokable<T, T> from(Constructor<T> constructor,
			TypeToken<T> receiver) {
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

	/**
	 * Create a new Invokable instance from a reference to a {@link Method}.
	 * 
	 * @param method
	 * @return An invokable wrapping the given method.
	 */
	public static Invokable<?, ?> from(Method method) {
		return from(method, TypeToken.of(method.getDeclaringClass()),
				TypeToken.of(method.getGenericReturnType()));
	}

	@SuppressWarnings("unchecked")
	static <T, R> Invokable<T, R> from(Method method, TypeToken<T> receiver,
			TypeToken<R> result) {
		return new Invokable<>(receiver, result, method, (T r, List<?> a) -> {
			try {
				return (R) method.invoke(r, a);
			} catch (Exception e) {
				throw new TypeInferenceException("Cannot invoke method '" + method
						+ "' on receiver '" + r + "' with arguments '" + a + "'.");
			}
		});
	}

	/**
	 * As invocation of {@link #from(Constructor)} or {@link Method} as
	 * appropriate.
	 * 
	 * @param executable
	 * @return An invokable wrapping the given Executable.
	 */
	public static Invokable<?, ?> from(Executable executable) {
		if (executable instanceof Method)
			return from((Method) executable);
		else
			return from((Constructor<?>) executable);
	}

	/**
	 * @return A copy of the {@link Resolver} instance backing this
	 *         {@link Invokable}.
	 */
	public Resolver getResolver() {
		return new Resolver(getInternalResolver());
	}

	private Resolver getInternalResolver() {
		return resolver;
	}

	/**
	 * @return True if the wrapped executable is abstract, false otherwise.
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is final, false otherwise.
	 */
	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is native, false otherwise.
	 */
	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is private, false otherwise.
	 */
	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is protected, false otherwise.
	 */
	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is public, false otherwise.
	 */
	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is static, false otherwise.
	 */
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is strict, false otherwise.
	 */
	public boolean isStrict() {
		return Modifier.isStrict(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is synchronized, false otherwise.
	 */
	public boolean isSynchronized() {
		return Modifier.isSynchronized(executable.getModifiers());
	}

	/**
	 * @return True if the wrapped executable is generic, false otherwise.
	 */
	public boolean isGeneric() {
		return executable.getTypeParameters().length > 0;
	}

	/**
	 * @return True if the wrapped executable is variable arity, false otherwise.
	 */
	public boolean isVariableArity() {
		return executable.isVarArgs();
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

		if (isGeneric()) {
			builder
					.append("<")
					.append(
							getTypeParameters().stream().map(getTypeArguments()::get)
									.map(Objects::toString).collect(Collectors.joining(", ")))
					.append("> ");
		}

		builder.append(returnType).toString();
		if (executable instanceof Method)
			builder.append(" ").append(receiverType).append(".")
					.append(executable.getName());

		return builder
				.append("(")
				.append(
						parameters.stream().map(Objects::toString)
								.collect(Collectors.joining(", "))).append(")").toString();
	}

	/**
	 * @return The executable represented by this {@link Invokable}.
	 */
	public Executable getExecutable() {
		return executable;
	}

	/**
	 * @return The type which declares this {@link Invokable}. As
	 *         {@link Executable#getDeclaringClass()}, but with any generic type
	 *         parameters resolved according to the type of
	 *         {@link #getReceiverType()}.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? super T> getDeclaringType() {
		return (TypeToken<? super T>) receiverType
				.resolveSupertypeParameters(executable.getDeclaringClass());
	}

	/**
	 * @return The exact receiving type of this invokable instance. Generic type
	 *         parameters may include inference variables.
	 */
	public TypeToken<T> getReceiverType() {
		return receiverType;
	}

	/**
	 * @return The exact return type of this invokable instance. Generic type
	 *         parameters may include inference variables.
	 */
	public TypeToken<R> getReturnType() {
		return returnType;
	}

	/**
	 * @return The exact types of the expected parameters of this invokable
	 *         instance. Inference variables may be mentioned.
	 */
	public List<Type> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	/**
	 * @param type
	 * @return As {@link #withReceiverType(Type)}.
	 */
	@SuppressWarnings("unchecked")
	public <U extends T> Invokable<U, ? extends R> withReceiverType(
			TypeToken<U> type) {
		return (Invokable<U, ? extends R>) withReceiverType(type.getType());
	}

	/**
	 * @param type
	 * @return A new instance of {@link Invokable} with the given receiver type.
	 *         This will resolve any overriding {@link Executable}, and possibly
	 *         substitute instantiations for inference variables or type variable
	 *         captures.
	 */
	public Invokable<? extends T, ? extends R> withReceiverType(Type type) {
		throw new UnsupportedOperationException(); // TODO resolve override
	}

	/**
	 * @param target
	 * @return As {@link #withTargetType(Type)}.
	 */
	public <S extends R> Invokable<T, S> withTargetType(Class<S> target) {
		return withTargetType(TypeToken.of(target));
	}

	/**
	 * @param target
	 * @return As {@link #withTargetType(Type)}.
	 */
	@SuppressWarnings("unchecked")
	public <S> Invokable<T, S> withTargetType(TypeToken<S> target) {
		return (Invokable<T, S>) withTargetType(target.getType());
	}

	/**
	 * @param target
	 * @return If the invocation type of this {@link Invokable} contains inference
	 *         variables such that the return type is not exactly known, this
	 *         method will attempt to create a new instance which is assignment
	 *         compatible with the given target.
	 */
	public Invokable<T, ? extends R> withTargetType(Type target) {
		return withTargetTypeCapture(target);
	}

	@SuppressWarnings("unchecked")
	private <S extends R> Invokable<T, S> withTargetTypeCapture(Type target) {
		Resolver resolver = getResolver();

		resolver.addLooseCompatibility(returnType.getType(), target);

		return new Invokable<>(resolver, receiverType, (TypeToken<S>) TypeToken.of(
				resolver, returnType.getType()), executable,
				(BiFunction<T, List<?>, S>) invocationFunction).infer();
	}

	/**
	 * @param targetType
	 * @param arguments
	 * @return As {@link #withInferredType(Type, Type...)}.
	 */
	public <U> Invokable<T, U> withInferredType(TypeToken<U> targetType,
			TypeToken<?>... arguments) {
		return withInferredType(targetType, Arrays.asList(arguments));
	}

	/**
	 * @param targetType
	 * @param arguments
	 * @return As {@link #withInferredType(Type, Type...)}.
	 */
	@SuppressWarnings("unchecked")
	public <U> Invokable<T, U> withInferredType(TypeToken<U> targetType,
			List<? extends TypeToken<?>> arguments) {
		return (Invokable<T, U>) withInferredType(targetType.getType(), arguments
				.stream().map(TypeToken::getType).collect(Collectors.toList()));
	}

	/**
	 * @param targetType
	 * @param arguments
	 * @return As {@link #withInferredType(Type, Type...)}.
	 */
	public Invokable<T, ? extends R> withInferredType(Type targetType,
			Type... arguments) {
		return withInferredType(targetType, Arrays.asList(arguments));
	}

	/**
	 * @param targetType
	 * @param arguments
	 * @return A new derived {@link Invokable} instance with inferred type such
	 *         that it is compatible with the given target type and arguments. If
	 *         an invocation is not possible with loose parameter applicability
	 *         and this is a variable arity method, an attempt is then made to
	 *         resolve with variable arity invocation.
	 */
	public Invokable<T, ? extends R> withInferredType(Type targetType,
			List<? extends Type> arguments) {
		Invokable<T, R> invokable;
		try {
			invokable = withLooseApplicability(arguments);
		} catch (Exception e) {
			if (isVariableArity())
				invokable = withVariableArityApplicability(arguments);
			else
				throw e;
		}
		return invokable.withTargetType(targetType);
	}

	/**
	 * @return A new derived {@link Invokable} instance with generic method
	 *         parameters inferred, and if this is a constructor on a generic
	 *         type, with generic type parameters inferred, too.
	 */
	public Invokable<T, R> infer() {
		Resolver resolver = getResolver();

		if (executable instanceof Method ? !resolver.validate(resolver
				.getInferenceVariables(executable).values()) : !resolver.validate())
			throw new TypeInferenceException(
					"Cannot resolve generic type parameters for invocation of '" + this
							+ "'.");

		return new Invokable<>(resolver, receiverType, returnType, executable,
				invocationFunction);
	}

	/**
	 * @param arguments
	 * @return As {@link #withStrictApplicability(Type...)}.
	 */
	public Invokable<T, R> withStrictApplicability(Type... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	/**
	 * @param arguments
	 * @return If the given parameters are not compatible with this invokable in a
	 *         strict compatibility invocation context, we return null. Otherwise,
	 *         we infer a partial parameterisation where necessary and return the
	 *         resulting derived invokable.
	 */
	public Invokable<T, R> withStrictApplicability(List<? extends Type> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/**
	 * @param arguments
	 * @return As {@link #withLooseApplicability(Type...)}.
	 */
	public Invokable<T, R> withLooseApplicability(Type... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	/**
	 * @param arguments
	 * @return If the given parameters are not compatible with this invokable in a
	 *         loose compatibility invocation context, we return null. Otherwise,
	 *         we infer a partial parameterisation where necessary and return the
	 *         resulting invokable.
	 */
	public Invokable<T, R> withLooseApplicability(List<? extends Type> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/**
	 * @param arguments
	 * @return As {@link #withVariableArityApplicability(Type...)}.
	 */
	public Invokable<T, R> withVariableArityApplicability(Type... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	/**
	 * @param arguments
	 * @return If the given parameters are not compatible with this invokable in a
	 *         loose compatibility invocation context, and with variable arity, we
	 *         return null. Otherwise, we infer a partial parameterisation where
	 *         necessary and return the resulting invokable.
	 */
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
			throw new TypeInferenceException(
					"Cannot resolve generic type parameters for invocation of '" + this
							+ "' with arguments '" + arguments + "'.");

		Resolver resolver = getResolver();

		if (!parameters.isEmpty()) {
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
				ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, argument,
						parameter, resolver.getBounds());
			}

			Resolver testResolver = new Resolver(resolver);

			if (!testResolver.validate())
				throw new TypeInferenceException(
						"Cannot resolve generic type parameters for invocation of '" + this
								+ "' with arguments '" + arguments + "'.");
		}

		return new Invokable<>(resolver, receiverType, returnType, executable,
				invocationFunction);
	}

	/**
	 * @return All generic type parameters of the wrapped {@link Executable}.
	 */
	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream()
				.map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	/**
	 * @return All generic type parameters instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	public Map<TypeVariable<? extends Executable>, Type> getTypeArguments() {
		return getTypeParameters().stream().collect(
				Collectors.toMap(t -> (TypeVariable<? extends Executable>) t,
						t -> resolver.resolveType(executable, t)));
	}

	/**
	 * @param variable
	 * @param instantiation
	 * @return A new derived {@link Invokable} instance with the given
	 *         instantiation substituted for the given type variable.
	 */
	public Invokable<T, ? extends R> withTypeArgument(
			TypeVariable<? extends Executable> variable, Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters())
				.anyMatch(variable::equals)) {
			Resolver resolver = new Resolver(this.resolver);
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	/**
	 * @param variable
	 * @param instantiation
	 * @return As {@link #withTypeArgument(TypeVariable, Type)}.
	 */
	@SuppressWarnings("unchecked")
	public <U> Invokable<T, ? extends R> withTypeArgument(
			TypeParameter<U> variable, TypeToken<U> instantiation) {
		return withTypeArgument(
				(TypeVariable<? extends Executable>) variable.getType(),
				instantiation.getType());
	}

	/**
	 * @param typeArguments
	 * @return A new derived {@link Invokable} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order.
	 */
	public Invokable<T, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	/**
	 * @param typeArguments
	 * @return As {@link #withTypeArguments(Type...)}.
	 */
	public Invokable<T, ? extends R> withTypeArguments(List<Type> typeArguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	/**
	 * <P>
	 * Invoke the wrapped {@link Executable} on the given receiver and with the
	 * given parameters. The receiver will be ignored for static methods or
	 * constructors. Variable arity invocation is not attempted.
	 * </p>
	 * 
	 * <p>
	 * Due to erasure of the types of the arguments, there is a limit to what type
	 * checking can be performed at runtime. For type safe invocation, wrap
	 * arguments in {@link TypedObject} instances and use an overload of
	 * {@link #invokeSafely(Object, TypedObject...)} instead.
	 * </p>
	 * 
	 * @param receiver
	 * @param arguments
	 * @return The result of the invocation.
	 */
	public R invoke(T receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	/**
	 * @param receiver
	 * @param arguments
	 * @return As {@link #invoke(Object, Object...)}.
	 */
	public R invoke(T receiver, List<? extends Object> arguments) {
		return invocationFunction.apply(receiver, arguments);
	}

	/**
	 * <p>
	 * As {@link #invoke(Object, Object...)}, but with arguments passed with their
	 * exact types, meaning full type checking can be performed at runtime. Also,
	 * here it is possible to determine whether the invocation is intended to be
	 * variable arity, and if so an attempt is made to invoke as such.
	 * </p>
	 * 
	 * <p>
	 * If the expected parameter types of this invokable contain inference
	 * variables or type variable captures, an attempt will be made to satisfy
	 * their bounds according to the given argument types.
	 * </p>
	 * 
	 * @param receiver
	 * @param arguments
	 * @return The result of the invocation.
	 */
	public R invokeSafely(T receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	/**
	 * @param receiver
	 * @param arguments
	 * @return As {@link #invokeSafely(Object, TypedObject...)}.
	 */
	public R invokeSafely(T receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getType().isAssignableTo(parameters.get(i)))
				throw new IllegalArgumentException("Argument '" + arguments.get(i)
						+ "' is not assignable to parameter '" + parameters.get(i)
						+ "' at index '" + i + "'.");
		return invoke(receiver, arguments);
	}

	/**
	 * Find the set of all given overload candidates which are applicable to
	 * invocation with the given parameters. Strict applicability is considered
	 * first, then if no candidates are found loose applicability is considered,
	 * then if still no candidates are found, variable arity applicability is
	 * considered.
	 * 
	 * @param candidates
	 * @param parameters
	 * @return The set of all given overload candidates which are applicable to
	 *         invocation with the given parameters.
	 */
	public static <T, R> Set<? extends Invokable<? super T, ? extends R>> resolveApplicableInvokables(
			Set<? extends Invokable<? super T, ? extends R>> candidates,
			List<? extends Type> parameters) {
		Map<Invokable<? super T, ? extends R>, RuntimeException> failures = new LinkedHashMap<>();
		BiConsumer<Invokable<? super T, ? extends R>, RuntimeException> putFailures = failures::put;

		Set<? extends Invokable<? super T, ? extends R>> compatibleCandidates = filterOverloadCandidates(
				candidates, i -> i.withLooseApplicability(parameters), putFailures);

		if (compatibleCandidates.isEmpty()) {
			compatibleCandidates = new HashSet<>(candidates);
			for (Invokable<? super T, ? extends R> candidate : candidates)
				if (!candidate.isVariableArity())
					compatibleCandidates.remove(candidate);

			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withVariableArityApplicability(parameters), putFailures);
		} else {
			Set<? extends Invokable<? super T, ? extends R>> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withStrictApplicability(parameters), putFailures);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw new TypeInferenceException("Parameters '" + parameters
					+ "' are not applicable to invokable candidates '" + candidates
					+ "'.", failures.get(failures.keySet().stream().findFirst().get()));

		return compatibleCandidates;
	}

	private static <T, R> Set<? extends Invokable<? super T, ? extends R>> filterOverloadCandidates(
			Collection<? extends Invokable<? super T, ? extends R>> candidates,
			Function<? super Invokable<? super T, ? extends R>, Invokable<? super T, ? extends R>> applicabilityFunction,
			BiConsumer<Invokable<? super T, ? extends R>, RuntimeException> failures) {
		return candidates.stream().map(i -> {
			try {
				return applicabilityFunction.apply(i);
			} catch (RuntimeException e) {
				failures.accept(i, e);
				return null;
			}
		}).filter(o -> o != null).collect(Collectors.toSet());
	}

	/**
	 * Find which of the given overload candidates is the most specific according
	 * to the rules described by the Java 8 language specification.
	 * 
	 * @param candidates
	 * @return The set of all given overload candidates which are applicable to
	 *         invocation with the given parameters.
	 */
	public static <T, R> Invokable<? super T, ? extends R> resolveMostSpecificInvokable(
			Collection<? extends Invokable<? super T, ? extends R>> candidates) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		Set<Invokable<? super T, ? extends R>> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<Invokable<? super T, ? extends R>> overrideCandidateIterator = mostSpecificSoFar
				.iterator();
		Invokable<? super T, ? extends R> mostSpecific = overrideCandidateIterator
				.next();
		while (overrideCandidateIterator.hasNext()) {
			Invokable<? super T, ? extends R> candidate = overrideCandidateIterator
					.next();

			if (!candidate.equals(mostSpecific))
				throw new TypeInferenceException(
						"Cannot resolve method invokation ambiguity between candidate '"
								+ candidate + "' and '" + mostSpecific + "'.");

			mostSpecific = candidate.getExecutable().getDeclaringClass()
					.isAssignableFrom(mostSpecific.getExecutable().getDeclaringClass()) ? candidate
					: mostSpecific;
		}

		return mostSpecific;
	}

	private static <T, R> Set<Invokable<? super T, ? extends R>> resolveMostSpecificCandidateSet(
			Collection<? extends Invokable<? super T, ? extends R>> candidates) {
		List<Invokable<? super T, ? extends R>> remainingCandidates = new ArrayList<>(
				candidates);

		/*
		 * For each remaining candidate in the list...
		 */
		for (int first = 0; first < remainingCandidates.size(); first++) {
			Invokable<? super T, ? extends R> firstCandidate = remainingCandidates
					.get(first);

			/*
			 * Compare with each other remaining candidate...
			 */
			for (int second = first + 1; second < remainingCandidates.size(); second++) {
				Invokable<? super T, ? extends R> secondCandidate = remainingCandidates
						.get(second);

				/*
				 * Determine which of the invokables, if either, are more specific.
				 */
				Pair<Boolean, Boolean> moreSpecific = compareCandidates(firstCandidate,
						secondCandidate);

				if (moreSpecific.getLeft()) {
					if (moreSpecific.getRight()) {
						/*
						 * First and second are equally specific.
						 */
					} else {
						/*
						 * First is strictly more specific.
						 */
						remainingCandidates.remove(second--);
					}
				} else if (moreSpecific.getRight()) {
					/*
					 * Second is strictly more specific.
					 */
					remainingCandidates.remove(first--);

					break;
				} else {
					/*
					 * Neither first nor second are more specific.
					 */
					throw new TypeInferenceException(
							"Cannot resolve method invokation ambiguity between candidate '"
									+ firstCandidate + "' and '" + secondCandidate + "'.");
				}
			}
		}

		return new HashSet<>(remainingCandidates);
	}

	private static Pair<Boolean, Boolean> compareCandidates(
			Invokable<?, ?> firstCandidate, Invokable<?, ?> secondCandidate) {
		boolean firstMoreSpecific = true;
		boolean secondMoreSpecific = true;

		if (firstCandidate.isGeneric())
			secondMoreSpecific = compareGenericCandidate(secondCandidate,
					firstCandidate);
		if (secondCandidate.isGeneric())
			firstMoreSpecific = compareGenericCandidate(firstCandidate,
					secondCandidate);

		if (!firstCandidate.isGeneric() || !secondCandidate.isGeneric()) {
			int i = 0;
			for (Type firstParameter : firstCandidate.getParameters()) {
				Type secondParameter = secondCandidate.getParameters().get(i++);

				if (!secondMoreSpecific && !secondCandidate.isGeneric()) {
					if (!Types.isAssignable(firstParameter, secondParameter)) {
						firstMoreSpecific = false;
						break;
					}
				} else if (!firstMoreSpecific && !firstCandidate.isGeneric()) {
					if (!Types.isAssignable(secondParameter, firstParameter)) {
						secondMoreSpecific = false;
						break;
					}
				} else {
					secondMoreSpecific = Types.isAssignable(secondParameter,
							firstParameter);
					firstMoreSpecific = Types.isAssignable(firstParameter,
							secondParameter);

					if (!(firstMoreSpecific || secondMoreSpecific))
						break;
				}
			}
		}

		return new Pair<Boolean, Boolean>(firstMoreSpecific, secondMoreSpecific);
	}

	private static boolean compareGenericCandidate(
			Invokable<?, ?> firstCandidate, Invokable<?, ?> genericCandidate) {
		Resolver resolver = genericCandidate.getResolver();

		try {
			int parameters = firstCandidate.getParameters().size();
			if (firstCandidate.isVariableArity()) {
				parameters--;

				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters()
						.get(parameters), genericCandidate.getParameters().get(parameters),
						resolver.getBounds());
			}

			for (int i = 0; i < parameters; i++) {
				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters()
						.get(i), genericCandidate.getParameters().get(i), resolver
						.getBounds());
			}

			resolver.validate();
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
