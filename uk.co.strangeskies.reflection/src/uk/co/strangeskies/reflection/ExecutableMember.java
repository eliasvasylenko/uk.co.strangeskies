/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
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
import uk.co.strangeskies.reflection.TypeToken.Wildcards;
import uk.co.strangeskies.utilities.tuple.Pair;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes. Instances of this class
 * can be created from instances of Executable directly from
 * {@link #over(Executable)} and its overloads, or using the
 * {@link TypeToken#resolveConstructorOverload(List)} and
 * {@link TypeToken#resolveMethodOverload(String, List)} methods on TypeToken
 * instances.
 * 
 * <p>
 * {@link ExecutableMember executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          The receiver type of the executable.
 * @param <R>
 *          The return type of the executable.
 */
public class ExecutableMember<O, R> implements TypeMember<O> {
	private final Resolver resolver;

	private final TypeToken<O> ownerType;
	private final TypeToken<R> returnType;
	private final Executable executable;

	private final List<Type> parameters;

	private final BiFunction<O, List<?>, R> invocationFunction;

	private final boolean variableArityInvocation;

	private ExecutableMember(TypeToken<O> receiverType, Executable executable,
			BiFunction<O, List<?>, R> invocationFunction) {
		this(receiverType.getResolver(), receiverType, executable, invocationFunction, null, false);
	}

	@SuppressWarnings("unchecked")
	private ExecutableMember(Resolver resolver, TypeToken<O> receiverType, Executable executable,
			BiFunction<O, List<?>, R> invocationFunction, List<? extends Type> parameters, boolean variableArityInvocation) {
		this.resolver = resolver;
		this.executable = executable;
		this.invocationFunction = invocationFunction;

		this.variableArityInvocation = variableArityInvocation;

		if (!isVariableArityDefinition() && isVariableArityInvocation()) {
			throw new TypeException(p -> p.invalidVariableArityInvocation(this));
		}

		/*
		 * Incorporate relevant type parameters:
		 */
		resolver.inferOverTypeParameters(getMember());

		/*
		 * Resolve types within context of given Resolver:
		 */
		if (isStatic())
			this.ownerType = (TypeToken<O>) TypeToken.over(receiverType.getRawType());
		else {
			if (!((receiverType.getType() instanceof ParameterizedType
					&& receiverType.getRawType().equals(executable.getDeclaringClass()))
					|| executable.getDeclaringClass().equals(receiverType.getType())))
				receiverType.resolveSupertypeParameters(executable.getDeclaringClass()).incorporateInto(resolver);

			receiverType = receiverType.withBoundsFrom(resolver).resolve();
			this.ownerType = receiverType;
		}

		if (executable instanceof Method) {
			Method method = (Method) executable;
			Type genericReturnType = resolver.resolveType(method, method.getGenericReturnType());

			// TODO should this always be PRESERVE?
			returnType = (TypeToken<R>) TypeToken.over(new Resolver(resolver.getBounds()), genericReturnType,
					InferenceVariable.isProperType(genericReturnType) ? Wildcards.PRESERVE : Wildcards.INFER);
			returnType.incorporateInto(resolver.getBounds());
		} else {
			returnType = (TypeToken<R>) receiverType;
		}

		/*
		 * Determine parameter types:
		 */

		Type[] genericParameters = executable.getGenericParameterTypes();
		for (int i = 0; i < genericParameters.length; i++) {
			genericParameters[i] = resolver.resolveType(genericParameters[i]);

			if (parameters != null) {
				Type givenArgument = resolver.resolveType(parameters.get(i));

				BoundSet bounds = new BoundSet();
				resolver.getBounds().getInferenceVariables().forEach(bounds::addInferenceVariable);

				Map<InferenceVariable, Type> captures = TypeVariableCapture
						.captureInferenceVariables(bounds.getInferenceVariables(), bounds);

				TypeSubstitution substitution = new TypeSubstitution(captures);

				Type givenArgumentCaptured = substitution.resolve(givenArgument);
				Type genericParameterCaptured = substitution.resolve(genericParameters[i]);

				if (Types.isAssignable(givenArgumentCaptured, genericParameterCaptured)) {
					genericParameters[i] = givenArgument;
				} else if (givenArgumentCaptured instanceof Class<?> && Types.isAssignable(givenArgumentCaptured,
						IntersectionType.from(Types.getRawTypes(genericParameterCaptured)))) {
					genericParameters[i] = givenArgument;
				} else if (!Types.isAssignable(genericParameterCaptured, givenArgumentCaptured)) {
					int finalI = i;
					throw new TypeException(
							p -> p.incompatibleArgument(givenArgumentCaptured, genericParameterCaptured, finalI, this));
				}
			}
		}
		this.parameters = Arrays.asList(genericParameters);
	}

	/**
	 * @return the name of the executable member
	 */
	@Override
	public String getName() {
		return getMember().getName();
	}

	@Override
	public String toString() {
		return toString(parameters);
	}

	private String toString(List<Type> parameters) {
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
			builder.append("<").append(getTypeParameters().stream().map(getTypeArguments()::get).map(Objects::toString)
					.collect(Collectors.joining(", "))).append("> ");
		}

		builder.append(returnType).toString();
		if (executable instanceof Method)
			builder.append(" ").append(ownerType).append(".").append(executable.getName());

		return builder.append("(").append(parameters.stream().map(Objects::toString).collect(Collectors.joining(", ")))
				.append(")").toString();
	}

	/**
	 * Create a new {@link ExecutableMember} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The type of the given {@link Constructor}.
	 * @param constructor
	 *          The constructor to wrap.
	 * @return An executable member wrapping the given constructor.
	 */
	public static <T> ExecutableMember<T, T> over(Constructor<T> constructor) {
		TypeToken<T> type = TypeToken.over(constructor.getDeclaringClass());
		return over(constructor, type);
	}

	/**
	 * Create a new {@link ExecutableMember} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The exact type of the constructor.
	 * @param constructor
	 *          The constructor to wrap.
	 * @param receiver
	 *          The target type of the given {@link Constructor}.
	 * @return An executable member wrapping the given constructor.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ExecutableMember<T, T> over(Constructor<? super T> constructor, TypeToken<T> receiver) {
		return new ExecutableMember<>(receiver, constructor, (T r, List<?> a) -> {
			try {
				return (T) constructor.newInstance(a.toArray());
			} catch (Exception e) {
				throw new TypeException(p -> p.invalidConstructorArguments(constructor, receiver, a), e);
			}
		});
	}

	/**
	 * Create a new {@link ExecutableMember} instance from a reference to a
	 * {@link Method}.
	 * 
	 * @param method
	 *          The method to wrap.
	 * @return An executable member wrapping the given method.
	 */
	public static ExecutableMember<?, ?> over(Method method) {
		TypeToken<?> receiver = TypeToken.over(method.getDeclaringClass());
		return over(method, receiver);
	}

	/**
	 * Create a new {@link ExecutableMember} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The exact type of the method receiver.
	 * @param method
	 *          The method to wrap.
	 * @param receiver
	 *          The target type of the given {@link Method}.
	 * @return An executable member wrapping the given method.
	 */
	public static <T> ExecutableMember<T, ?> over(Method method, TypeToken<T> receiver) {
		return new ExecutableMember<>(receiver, method, (T r, List<?> a) -> {
			try {
				return method.invoke(r, a.toArray());
			} catch (Exception e) {
				throw new TypeException(p -> p.invalidMethodArguments(method, receiver, a), e);
			}
		});
	}

	/**
	 * As invocation of {@link #over(Constructor)} or {@link Method} as
	 * appropriate.
	 * 
	 * @param executable
	 *          The executable to wrap.
	 * @return An executable member wrapping the given Executable.
	 */
	public static ExecutableMember<?, ?> over(Executable executable) {
		if (executable instanceof Method)
			return over((Method) executable);
		else
			return over((Constructor<?>) executable);
	}

	/**
	 * As invocation of {@link #over(Constructor)} or {@link Method} as
	 * appropriate.
	 * 
	 * @param <T>
	 *          The target type of the given {@link Executable}.
	 * @param executable
	 *          The executable to wrap.
	 * @param receiver
	 *          The type of the constructor or method receiver.
	 * @return An executable member wrapping the given Executable.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ExecutableMember<T, ?> over(Executable executable, TypeToken<T> receiver) {
		if (executable instanceof Method)
			return over((Method) executable, receiver);
		else
			return over((Constructor<? super T>) executable, receiver);
	}

	@Override
	public Resolver getResolver() {
		return getInternalResolver().copy();
	}

	private Resolver getInternalResolver() {
		return resolver;
	}

	/**
	 * @return true if the wrapped executable is abstract, false otherwise
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is native, false otherwise
	 */
	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	@Override
	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is strict, false otherwise
	 */
	public boolean isStrict() {
		return Modifier.isStrict(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is synchronized, false otherwise
	 */
	public boolean isSynchronized() {
		return Modifier.isSynchronized(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is generic, false otherwise
	 */
	public boolean isGeneric() {
		return executable.getTypeParameters().length > 0;
	}

	/**
	 * @return true if the wrapped executable is variable arity, false otherwise
	 */
	public boolean isVariableArityDefinition() {
		return executable.isVarArgs();
	}

	/**
	 * Check whether the executable is flagged to be invoked with varargs. If an
	 * executable is flagged to be invoked with varargs, then the
	 * {@link #invoke(Object, List) invocation} will be made by putting trailing
	 * arguments into an array as per Java variable arity method invocation rules.
	 * 
	 * @return true if the executable is flagged to be invoked with varargs, false
	 *         otherwise
	 */
	public boolean isVariableArityInvocation() {
		return variableArityInvocation;
	}

	/**
	 * @return copy of the {@link ExecutableMember} flagged to be invoked with
	 *         {@link #isVariableArityInvocation() variable arity}
	 */
	public ExecutableMember<O, R> asVariableArityInvocation() {
		if (isVariableArityInvocation()) {
			return this;
		} else {
			return new ExecutableMember<>(resolver, ownerType, executable, invocationFunction, parameters, true);
		}
	}

	/**
	 * @return The executable represented by this {@link ExecutableMember}.
	 */
	@Override
	public Executable getMember() {
		return executable;
	}

	@Override
	public TypeToken<O> getOwnerType() {
		return ownerType;
	}

	/**
	 * @return The exact return type of this executable member instance. Generic
	 *         type parameters may include inference variables.
	 */
	public TypeToken<R> getReturnType() {
		return returnType;
	}

	/**
	 * @return The exact types of the expected parameters of this executable
	 *         member instance. Inference variables may be mentioned.
	 */
	public List<Type> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	/**
	 * Derive a new {@link ExecutableMember} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link ExecutableMember} will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @return The newly derived {@link ExecutableMember}.
	 */
	@Override
	public ExecutableMember<O, R> withBounds(BoundSet bounds) {
		return withBounds(bounds, bounds.getInferenceVariables());
	}

	/**
	 * Derive a new {@link ExecutableMember} instance, with the bounds on the
	 * given inference variables, with respect to the given bound set,
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link ExecutableMember} will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @param inferenceVariables
	 *          The new inference variables whose bounds are to be incorporated.
	 * @return The newly derived {@link ExecutableMember}.
	 */
	@Override
	public ExecutableMember<O, R> withBounds(BoundSet bounds,
			Collection<? extends InferenceVariable> inferenceVariables) {
		Resolver resolver = getResolver();
		resolver.getBounds().incorporate(bounds, inferenceVariables);

		return new ExecutableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableMember} instance, with the bounds on the
	 * given type incorporated into the bounds of the underlying resolver. The
	 * original {@link ExecutableMember} will remain unmodified.
	 * 
	 * @param type
	 *          The type whose bounds are to be incorporated.
	 * @return The newly derived {@link ExecutableMember}.
	 */
	@Override
	public ExecutableMember<O, R> withBoundsFrom(TypeToken<?> type) {
		return withBounds(type.getResolver().getBounds(), type.getInferenceVariablesMentioned());
	}

	/**
	 * Derive a new instance of {@link ExecutableMember} with the given receiver
	 * type. This will resolve any overriding {@link Executable} to determine a
	 * new return type if necessary.
	 * 
	 * <p>
	 * The new {@link ExecutableMember} will always have a receiver type which is
	 * as or more specific than both the current receiver type <em>and</em> the
	 * given type. This means that the new receiver will be assignment compatible
	 * with the given type, but if the given type contains wildcards or inference
	 * variables which are less specific that those implied by the
	 * <em>current</em> receiver type, new type arguments will be inferred in
	 * their place, or further bounds may be added to them.
	 * 
	 * @param <U>
	 *          The new receiver type. The raw type of this type must be a subtype
	 *          of the raw type of the current receiver type.
	 * @param type
	 *          The new receiver type. The raw type of this type must be a subtype
	 *          of the raw type of the current receiver type.
	 * @return A new {@link ExecutableMember} compatible with the given receiver
	 *         type.
	 * 
	 *         <p>
	 *         The new receiver type will not be effectively more specific than
	 *         the intersection type of the current receiver type and the given
	 *         type. That is, any type which can be assigned to both the given
	 *         type and the current receiver type, will also be assignable to the
	 *         new type.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <U extends O> ExecutableMember<U, ? extends R> withOwnerType(TypeToken<U> type) {
		return (ExecutableMember<U, ? extends R>) withBoundsFrom(type).withOwnerType(type.getType());
	}

	/**
	 * Derive a new instance of {@link ExecutableMember} with the given receiver
	 * type. This will resolve any overriding {@link Executable} to determine a
	 * new return type if necessary.
	 * 
	 * <p>
	 * The new {@link ExecutableMember} will always have a receiver type which is
	 * as or more specific than both the current receiver type <em>and</em> the
	 * given type. This means that the new receiver will be assignment compatible
	 * with the given type, but if the given type contains wildcards or inference
	 * variables which are less specific that those implied by the
	 * <em>current</em> receiver type, new type arguments will be inferred in
	 * their place, or further bounds may be added to them.
	 * 
	 * @param type
	 *          The new receiver type. The raw type of this type must be a subtype
	 *          of the raw type of the current receiver type.
	 * @return A new {@link ExecutableMember} compatible with the given receiver
	 *         type.
	 * 
	 *         <p>
	 *         The new receiver type will not be effectively more specific than
	 *         the intersection type of the current receiver type and the given
	 *         type. That is, any type which can be assigned to both the given
	 *         type and the current receiver type, will also be assignable to the
	 *         new type.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ExecutableMember<? extends O, ? extends R> withOwnerType(Type type) {
		try {
			Resolver resolver = getResolver();

			ConstraintFormula.reduce(Kind.SUBTYPE, type, ownerType.getType(), resolver.getBounds());

			Class<?> mostSpecificOverridingClass = this.executable.getDeclaringClass();
			for (Class<?> candidate : resolver.getRawTypes(type))
				if (mostSpecificOverridingClass.isAssignableFrom(candidate))
					mostSpecificOverridingClass = candidate;

			Executable override = mostSpecificOverridingClass.equals(executable.getDeclaringClass()) ? executable
					: mostSpecificOverridingClass.getMethod(executable.getName(), executable.getParameterTypes());

			return new ExecutableMember<>(resolver, (TypeToken<O>) TypeToken.over(type), override, invocationFunction,
					parameters, variableArityInvocation);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new TypeException(p -> p.cannotResolveOverride(this, type), e);
		}
	}

	/**
	 * Derive a new instance of {@link ExecutableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public <S extends R> ExecutableMember<O, S> withTargetType(Class<S> target) {
		return withTargetType(TypeToken.over(target));
	}

	/**
	 * Derive a new instance of {@link ExecutableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	@SuppressWarnings("unchecked")
	public <S> ExecutableMember<O, S> withTargetType(TypeToken<S> target) {
		if (target == null)
			return (ExecutableMember<O, S>) this;

		return (ExecutableMember<O, S>) withBoundsFrom(target).withTargetType(target.getType());
	}

	/**
	 * Derive a new instance of {@link ExecutableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param target
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public ExecutableMember<O, ? extends R> withTargetType(Type target) {
		return withTargetTypeCapture(target);
	}

	@SuppressWarnings("unchecked")
	private <S extends R> ExecutableMember<O, S> withTargetTypeCapture(Type target) {
		if (target == null)
			return (ExecutableMember<O, S>) this;

		Resolver resolver = getResolver();

		ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, returnType.getType(), target, resolver.getBounds());

		return new ExecutableMember<>(resolver, ownerType, executable, (BiFunction<O, List<?>, S>) invocationFunction,
				parameters, variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableMember} fulfilling two conditions.
	 * 
	 * <ul>
	 * <li>Firstly, that the result be assignment compatible with the given target
	 * type.</li>
	 * <li>Secondly, that the arguments are compatible in either a
	 * {@link #withLooseApplicability(List) loose invocation context}, or failing
	 * that, a {@link #withVariableArityApplicability(List) variable arity
	 * invocation context}.</li>
	 * </ul>
	 * 
	 * <p>
	 * This method uses the same type inference algorithm as the Java language,
	 * and so will only fail in those cases where the Java compiler would have
	 * failed to infer a type.
	 * 
	 * @param <U>
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @param targetType
	 *          The derived {@link ExecutableMember} must be assignment compatible
	 *          with this type.
	 * @param arguments
	 *          The derived {@link ExecutableMember} must be loose invocation
	 *          compatible, or failing that variable arity compatible, with the
	 *          given arguments.
	 * @return A new {@link ExecutableMember} compatible with the given target
	 *         type and parameters, and which has more specific arguments, type
	 *         arguments, and return type than the receiving
	 *         {@link ExecutableMember}.
	 */
	public <U> ExecutableMember<O, U> withInferredType(TypeToken<U> targetType, TypeToken<?>... arguments) {
		ExecutableMember<O, R> executableMember;
		try {
			executableMember = withLooseApplicability(arguments);
		} catch (Exception e) {
			if (isVariableArityDefinition())
				executableMember = withVariableArityApplicability(arguments);
			else
				throw e;
		}
		return executableMember.withTargetType(targetType);
	}

	/**
	 * Derived a new {@link ExecutableMember} instance with generic method
	 * parameters inferred, and if this is a member of a generic type, with
	 * generic type parameters inferred, too.
	 * 
	 * @return The derived {@link ExecutableMember} with inferred invocation type.
	 */
	@Override
	public ExecutableMember<O, R> infer() {
		Resolver resolver = getResolver();

		resolver.infer();

		return new ExecutableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * @return A new derived {@link ExecutableMember} instance with generic method
	 *         parameters inferred, and if this is a constructor on a generic
	 *         type, with generic type parameters inferred, also.
	 */
	public ExecutableMember<O, R> inferParameterTypes() {
		Resolver resolver = getResolver();

		for (Type parameter : parameters)
			resolver.infer(parameter);

		return new ExecutableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a strict
	 * invocation context. Where necessary, the derived {@link ExecutableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withStrictApplicability(TypeToken<?>... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a strict
	 * invocation context. Where necessary, the derived {@link ExecutableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withStrictApplicability(List<? extends TypeToken<?>> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a loose
	 * invocation context. Where necessary, the derived {@link ExecutableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withLooseApplicability(TypeToken<?>... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a loose
	 * invocation context. Where necessary, the derived {@link ExecutableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withLooseApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a variable
	 * arity invocation context. Where necessary, the derived
	 * {@link ExecutableMember} may infer new bounds or instantiations on type
	 * parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withVariableArityApplicability(TypeToken<?>... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with inferred invocation
	 * type such that it is compatible with the given arguments in a variable
	 * arity invocation context. Where necessary, the derived
	 * {@link ExecutableMember} may infer new bounds or instantiations on type
	 * parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableMember}.
	 */
	public ExecutableMember<O, R> withVariableArityApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(true, arguments);
	}

	private ExecutableMember<O, R> withLooseApplicability(boolean variableArity, List<? extends TypeToken<?>> arguments) {
		if (variableArity) {
			if (!executable.isVarArgs())
				throw new TypeException(p -> p.invalidVariableArityInvocation(this));

			if (parameters.size() > arguments.size() + 1)
				throw new TypeException(p -> p.cannotResolveInvocationType(this, arguments));
		} else if (parameters.size() != arguments.size())
			throw new TypeException(p -> p.cannotResolveInvocationType(this, arguments));

		Resolver resolver = getResolver();

		if (!parameters.isEmpty()) {
			Iterator<Type> parameters = this.parameters.iterator();
			Type nextParameter = parameters.next();
			Type parameter = nextParameter;
			for (TypeToken<?> argument : arguments) {
				if (nextParameter != null) {
					parameter = nextParameter;
					if (parameters.hasNext())
						nextParameter = parameters.next();
					else if (variableArity) {
						parameter = Types.getComponentType(parameter);
						nextParameter = null;
					}
				}

				argument.incorporateInto(resolver.getBounds());

				ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, argument.getType(), parameter, resolver.getBounds());
			}

			// Test resolution is possible.
			resolver.copy().infer();
		}

		return new ExecutableMember<>(resolver, ownerType, executable, invocationFunction, parameters, variableArity);
	}

	/**
	 * @return All generic type parameters of the wrapped {@link Executable}.
	 */
	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream().map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	/**
	 * @return All generic type parameters instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	public Map<TypeVariable<? extends Executable>, Type> getTypeArguments() {
		return getTypeParameters().stream().collect(Collectors.toMap(t -> t, t -> resolver.resolveType(executable, t)));
	}

	/**
	 * Derive a new {@link ExecutableMember} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * @param variable
	 *          The type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableMember}.
	 * @param instantiation
	 *          The type with which to instantiate the given type variable.
	 * @return A new derived {@link ExecutableMember} instance with the given
	 *         instantiation substituted for the given type variable.
	 */
	public ExecutableMember<O, ? extends R> withTypeArgument(TypeVariable<? extends Executable> variable,
			Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters()).anyMatch(variable::equals)) {
			Resolver resolver = this.resolver.copy();
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	/**
	 * Derive a new {@link ExecutableMember} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * @param <U>
	 *          The type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableMember}.
	 * @param variable
	 *          The type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableMember}.
	 * @param instantiation
	 *          The type with which to instantiate the given type variable.
	 * @return A new derived {@link ExecutableMember} instance with the given
	 *         instantiation substituted for the given type variable.
	 */
	@SuppressWarnings("unchecked")
	public <U> ExecutableMember<O, ? extends R> withTypeArgument(TypeParameter<U> variable, TypeToken<U> instantiation) {
		return withTypeArgument((TypeVariable<? extends Executable>) variable.getType(), instantiation.getType());
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with the given generic type
	 * argument substitutions, as per the behaviour of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          A list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}.
	 * @return A new derived {@link ExecutableMember} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order.
	 */
	public ExecutableMember<O, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	/**
	 * Derive a new {@link ExecutableMember} instance with the given generic type
	 * argument substitutions, as per the behaviour of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          A list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}.
	 * @return A new derived {@link ExecutableMember} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order.
	 */
	public ExecutableMember<O, ? extends R> withTypeArguments(List<Type> typeArguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	/**
	 * Invoke the wrapped {@link Executable} on the given receiver and with the
	 * given parameters. The receiver will be ignored for static methods or
	 * constructors. Variable arity invocation is not attempted.
	 * 
	 * <p>
	 * Due to erasure of the types of the arguments, there is a limit to what type
	 * checking can be performed at runtime. For type safe invocation, wrap
	 * arguments in {@link TypedObject} instances and use an overload of
	 * {@link #invokeSafely(Object, TypedObject...)} instead.
	 * 
	 * 
	 * @param receiver
	 *          The receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation.
	 * @param arguments
	 *          The argument list for the invocation.
	 * @return The result of the invocation.
	 */
	public R invoke(O receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	/**
	 * Invoke the wrapped {@link Executable} on the given receiver and with the
	 * given parameters. The receiver will be ignored for static methods or
	 * constructors. Variable arity invocation is not attempted.
	 * 
	 * <p>
	 * Due to erasure of the types of the arguments, there is a limit to what type
	 * checking can be performed at runtime. For type safe invocation, wrap
	 * arguments in {@link TypedObject} instances and use an overload of
	 * {@link #invokeSafely(Object, TypedObject...)} instead.
	 * 
	 * 
	 * @param receiver
	 *          The receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation.
	 * @param arguments
	 *          The argument list for the invocation.
	 * @return The result of the invocation.
	 */
	public R invoke(O receiver, List<? extends Object> arguments) {
		if (variableArityInvocation) {
			List<Object> actualArguments = new ArrayList<>(parameters.size());
			Object[] varargs = (Object[]) Array.newInstance(
					Types.getRawType(parameters.get(parameters.size() - 1)).getComponentType(),
					arguments.size() - parameters.size() + 1);

			for (int i = 0; i < parameters.size() - 1; i++) {
				actualArguments.add(arguments.get(0));
			}

			int j = 0;
			for (int i = parameters.size() - 1; i < arguments.size(); i++) {
				varargs[j++] = arguments.get(i);
			}
			actualArguments.add(varargs);

			return invocationFunction.apply(receiver, actualArguments);
		} else {
			return invocationFunction.apply(receiver, arguments);
		}
	}

	/**
	 * <p>
	 * As {@link #invoke(Object, Object...)}, but with arguments passed with their
	 * exact types, meaning full type checking can be performed at runtime. Also,
	 * here it is possible to determine whether the invocation is intended to be
	 * variable arity, and if so an attempt is made to invoke as such.
	 * 
	 * <p>
	 * If the expected parameter types of this executable member contain inference
	 * variables or type variable captures, an attempt will be made to satisfy
	 * their bounds according to the given argument types.
	 * 
	 * @param receiver
	 *          The receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation.
	 * @param arguments
	 *          The typed argument list for the invocation.
	 * @return The result of the invocation.
	 */
	public R invokeSafely(O receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	/**
	 * <p>
	 * As {@link #invoke(Object, Object...)}, but with arguments passed with their
	 * exact types, meaning full type checking can be performed at runtime. Also,
	 * here it is possible to determine whether the invocation is intended to be
	 * variable arity, and if so an attempt is made to invoke as such.
	 * 
	 * <p>
	 * If the expected parameter types of this executable member contain inference
	 * variables or type variable captures, an attempt will be made to satisfy
	 * their bounds according to the given argument types.
	 * 
	 * @param receiver
	 *          The receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation.
	 * @param arguments
	 *          The typed argument list for the invocation.
	 * @return The result of the invocation.
	 */
	public R invokeSafely(O receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getType().isAssignableTo(parameters.get(i))) {
				int finalI = i;
				throw new TypeException(
						p -> p.incompatibleArgument(arguments.get(finalI), parameters.get(finalI), finalI, this));
			}
		return invoke(receiver, arguments);
	}

	/**
	 * Find the set of all given overload candidates which are applicable to
	 * invocation with the given parameters. Strict applicability is considered
	 * first, then if no candidates are found loose applicability is considered,
	 * then if still no candidates are found, variable arity applicability is
	 * considered.
	 * 
	 * @param <T>
	 *          The receiving type of the given executable members.
	 * @param <R>
	 *          The return type of the given executable members.
	 * @param candidates
	 *          The candidates for which we wish to determine applicability.
	 * @param parameters
	 *          The parameters representing the invocation for which we wish to
	 *          determine applicability.
	 * @return The set of all given overload candidates which are most applicable
	 *         to invocation with the given parameters.
	 */
	public static <T, R> Set<? extends ExecutableMember<? super T, ? extends R>> resolveApplicableExecutableMembers(
			Set<? extends ExecutableMember<? super T, ? extends R>> candidates, List<? extends TypeToken<?>> parameters) {
		Map<ExecutableMember<? super T, ? extends R>, RuntimeException> failures = new LinkedHashMap<>();
		BiConsumer<ExecutableMember<? super T, ? extends R>, RuntimeException> putFailures = failures::put;

		Set<? extends ExecutableMember<? super T, ? extends R>> compatibleCandidates = filterOverloadCandidates(candidates,
				i -> i.withLooseApplicability(parameters), putFailures);

		if (compatibleCandidates.isEmpty()) {
			compatibleCandidates = new HashSet<>(candidates);
			for (ExecutableMember<? super T, ? extends R> candidate : candidates)
				if (!candidate.isVariableArityDefinition())
					compatibleCandidates.remove(candidate);

			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withVariableArityApplicability(parameters), putFailures);
		} else {
			Set<? extends ExecutableMember<? super T, ? extends R>> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates, i -> i.withStrictApplicability(parameters),
					putFailures);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw new TypeException(p -> p.cannotResolveApplicable(candidates, parameters),
					failures.get(failures.keySet().stream().findFirst().get()));

		return compatibleCandidates;
	}

	private static <T, R> Set<? extends ExecutableMember<? super T, ? extends R>> filterOverloadCandidates(
			Collection<? extends ExecutableMember<? super T, ? extends R>> candidates,
			Function<? super ExecutableMember<? super T, ? extends R>, ExecutableMember<? super T, ? extends R>> applicabilityFunction,
			BiConsumer<ExecutableMember<? super T, ? extends R>, RuntimeException> failures) {
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
	 * <p>
	 * If no single most specific candidate can be found, the method will throw a
	 * {@link TypeException}.
	 * 
	 * @param <T>
	 *          The receiving type of the given executable members.
	 * @param <R>
	 *          The return type of the given executable members.
	 * @param candidates
	 *          The candidates from which to select the most specific.
	 * @return The most specific of the given candidates.
	 */
	public static <T, R> ExecutableMember<? super T, ? extends R> resolveMostSpecificExecutableMember(
			Collection<? extends ExecutableMember<? super T, ? extends R>> candidates) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		Set<ExecutableMember<? super T, ? extends R>> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<ExecutableMember<? super T, ? extends R>> overrideCandidateIterator = mostSpecificSoFar.iterator();
		ExecutableMember<? super T, ? extends R> mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			ExecutableMember<? super T, ? extends R> candidate = overrideCandidateIterator.next();

			if (!candidate.getParameters().equals(mostSpecific.getParameters())
					|| !candidate.getName().equals(mostSpecific.getName())) {
				ExecutableMember<?, ?> mostSpecificFinal = mostSpecific;
				throw new TypeException(p -> p.cannotResolveAmbiguity(candidate, mostSpecificFinal));
			}

			mostSpecific = candidate.getMember().getDeclaringClass()
					.isAssignableFrom(mostSpecific.getMember().getDeclaringClass()) ? candidate : mostSpecific;
		}

		return mostSpecific;
	}

	private static <T, R> Set<ExecutableMember<? super T, ? extends R>> resolveMostSpecificCandidateSet(
			Collection<? extends ExecutableMember<? super T, ? extends R>> candidates) {
		List<ExecutableMember<? super T, ? extends R>> remainingCandidates = new ArrayList<>(candidates);

		/*
		 * For each remaining candidate in the list...
		 */
		for (int first = 0; first < remainingCandidates.size(); first++) {
			ExecutableMember<? super T, ? extends R> firstCandidate = remainingCandidates.get(first);

			/*
			 * Compare with each other remaining candidate...
			 */
			for (int second = first + 1; second < remainingCandidates.size(); second++) {
				ExecutableMember<? super T, ? extends R> secondCandidate = remainingCandidates.get(second);

				/*
				 * Determine which of the executable members, if either, are more
				 * specific.
				 */
				Pair<Boolean, Boolean> moreSpecific = compareCandidates(firstCandidate, secondCandidate);

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
					throw new TypeException(p -> p.cannotResolveAmbiguity(firstCandidate, secondCandidate));
				}
			}
		}

		return new HashSet<>(remainingCandidates);
	}

	private static Pair<Boolean, Boolean> compareCandidates(ExecutableMember<?, ?> firstCandidate,
			ExecutableMember<?, ?> secondCandidate) {
		boolean firstMoreSpecific = true;
		boolean secondMoreSpecific = true;

		if (firstCandidate.isGeneric())
			secondMoreSpecific = compareGenericCandidate(secondCandidate, firstCandidate);
		if (secondCandidate.isGeneric())
			firstMoreSpecific = compareGenericCandidate(firstCandidate, secondCandidate);

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
					secondMoreSpecific = Types.isAssignable(secondParameter, firstParameter);
					firstMoreSpecific = Types.isAssignable(firstParameter, secondParameter);

					if (!(firstMoreSpecific || secondMoreSpecific))
						break;
				}
			}
		}

		return new Pair<>(firstMoreSpecific, secondMoreSpecific);
	}

	private static boolean compareGenericCandidate(ExecutableMember<?, ?> firstCandidate,
			ExecutableMember<?, ?> genericCandidate) {
		Resolver resolver = genericCandidate.getResolver();

		try {
			int parameters = firstCandidate.getParameters().size();
			if (firstCandidate.isVariableArityDefinition()) {
				parameters--;

				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters().get(parameters),
						genericCandidate.getParameters().get(parameters), resolver.getBounds());
			}

			for (int i = 0; i < parameters; i++) {
				ConstraintFormula.reduce(Kind.SUBTYPE, firstCandidate.getParameters().get(i),
						genericCandidate.getParameters().get(i), resolver.getBounds());
			}

			resolver.infer();
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
