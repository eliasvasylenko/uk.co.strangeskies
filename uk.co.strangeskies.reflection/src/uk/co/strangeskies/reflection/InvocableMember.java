/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. l      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' l   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
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

import static java.util.Arrays.stream;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.TypeToken.Wildcards;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link InvocableMember executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          The receiver type of the executable.
 * @param <R>
 *          The return type of the executable.
 */
public class InvocableMember<O, R> implements TypeMember<O> {
	private final TypeResolver resolver;

	private final TypeToken<O> ownerType;
	private final TypeToken<R> returnType;
	private final Executable executable;

	private final List<Type> parameters;

	private final BiFunction<O, List<?>, R> invocationFunction;

	private final boolean variableArityInvocation;

	private InvocableMember(TypeToken<O> receiverType, Executable executable,
			BiFunction<O, List<?>, R> invocationFunction) {
		this(receiverType.getResolver(), receiverType, executable, invocationFunction, null, false);
	}

	@SuppressWarnings("unchecked")
	private InvocableMember(TypeResolver resolver, TypeToken<O> receiverType, Executable executable,
			BiFunction<O, List<?>, R> invocationFunction, List<? extends Type> parameters, boolean variableArityInvocation) {
		this.resolver = resolver;
		this.executable = executable;
		this.invocationFunction = invocationFunction;

		this.variableArityInvocation = variableArityInvocation;

		if (!isVariableArityDefinition() && isVariableArityInvocation()) {
			throw new ReflectionException(p -> p.invalidVariableArityInvocation(this));
		}

		/*
		 * Incorporate relevant type parameters:
		 */
		resolver.inferOverTypeParameters(getMember());

		/*
		 * Resolve types within context of given Resolver:
		 */
		if (isStatic() || executable instanceof Constructor<?>)
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
			returnType = (TypeToken<R>) TypeToken.over(new TypeResolver(resolver.getBounds()), genericReturnType,
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
					throw new ReflectionException(
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
	 * Create a new {@link InvocableMember} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The type of the given {@link Constructor}.
	 * @param constructor
	 *          The constructor to wrap.
	 * @return An executable member wrapping the given constructor.
	 */
	public static <T> InvocableMember<Void, T> over(Constructor<T> constructor) {
		return over(constructor, TypeToken.over(constructor.getDeclaringClass()));
	}

	/**
	 * Create a new {@link InvocableMember} instance from a reference to a
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
	public static <T> InvocableMember<Void, T> over(Constructor<? super T> constructor, TypeToken<T> receiver) {
		return new InvocableMember<>(TypeToken.over(void.class), constructor, (Void r, List<?> a) -> {
			try {
				return (T) constructor.newInstance(a.toArray());
			} catch (Exception e) {
				throw new ReflectionException(p -> p.invalidConstructorArguments(constructor, receiver, a), e);
			}
		});
	}

	/**
	 * Create a new {@link InvocableMember} instance from a reference to a
	 * {@link Method}.
	 * 
	 * @param method
	 *          The method to wrap.
	 * @return An executable member wrapping the given method.
	 */
	public static InvocableMember<?, ?> over(Method method) {
		TypeToken<?> receiver = TypeToken.over(method.getDeclaringClass());
		return over(method, receiver);
	}

	/**
	 * Create a new {@link InvocableMember} instance from a reference to a
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
	public static <T> InvocableMember<T, ?> over(Method method, TypeToken<T> receiver) {
		return new InvocableMember<>(receiver, method, (T r, List<?> a) -> {
			try {
				return method.invoke(r, a.toArray());
			} catch (Exception e) {
				throw new ReflectionException(p -> p.invalidMethodArguments(method, receiver, a), e);
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
	public static InvocableMember<?, ?> over(Executable executable) {
		if (executable instanceof Method)
			return over((Method) executable);
		else
			return over((Constructor<?>) executable);
	}

	@Override
	public TypeResolver getResolver() {
		return getInternalResolver().copy();
	}

	private TypeResolver getInternalResolver() {
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
	 * @return copy of the {@link InvocableMember} flagged to be invoked with
	 *         {@link #isVariableArityInvocation() variable arity}
	 */
	public InvocableMember<O, R> asVariableArityInvocation() {
		if (isVariableArityInvocation()) {
			return this;
		} else {
			return new InvocableMember<>(resolver, ownerType, executable, invocationFunction, parameters, true);
		}
	}

	/**
	 * @return The executable represented by this {@link InvocableMember}.
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
	 * Derive a new {@link InvocableMember} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link InvocableMember} will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @return The newly derived {@link InvocableMember}.
	 */
	@Override
	public InvocableMember<O, R> withBounds(BoundSet bounds) {
		return withBounds(bounds, bounds.getInferenceVariables());
	}

	/**
	 * Derive a new {@link InvocableMember} instance, with the bounds on the given
	 * inference variables, with respect to the given bound set, incorporated into
	 * the bounds of the underlying resolver. The original {@link InvocableMember}
	 * will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @param inferenceVariables
	 *          The new inference variables whose bounds are to be incorporated.
	 * @return The newly derived {@link InvocableMember}.
	 */
	@Override
	public InvocableMember<O, R> withBounds(BoundSet bounds, Collection<? extends InferenceVariable> inferenceVariables) {
		TypeResolver resolver = getResolver();
		resolver.getBounds().incorporate(bounds, inferenceVariables);

		return new InvocableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * Derive a new {@link InvocableMember} instance, with the bounds on the given
	 * type incorporated into the bounds of the underlying resolver. The original
	 * {@link InvocableMember} will remain unmodified.
	 * 
	 * @param type
	 *          The type whose bounds are to be incorporated.
	 * @return The newly derived {@link InvocableMember}.
	 */
	@Override
	public InvocableMember<O, R> withBoundsFrom(TypeToken<?> type) {
		return withBounds(type.getResolver().getBounds(), type.getInferenceVariablesMentioned());
	}

	/**
	 * Derive a new instance of {@link InvocableMember} with the given receiver
	 * type. This will resolve any overriding {@link Executable} to determine a
	 * new return type if necessary.
	 * 
	 * <p>
	 * The new {@link InvocableMember} will always have a receiver type which is
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
	 * @return A new {@link InvocableMember} compatible with the given receiver
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
	public <U extends O> InvocableMember<U, ? extends R> withOwnerType(TypeToken<U> type) {
		return (InvocableMember<U, ? extends R>) withBoundsFrom(type).withOwnerType(type.getType());
	}

	/**
	 * Derive a new instance of {@link InvocableMember} with the given receiver
	 * type. This will resolve any overriding {@link Executable} to determine a
	 * new return type if necessary.
	 * 
	 * <p>
	 * The new {@link InvocableMember} will always have a receiver type which is
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
	 * @return A new {@link InvocableMember} compatible with the given receiver
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
	public InvocableMember<? extends O, ? extends R> withOwnerType(Type type) {
		try {
			TypeResolver resolver = getResolver();

			ConstraintFormula.reduce(Kind.SUBTYPE, type, ownerType.getType(), resolver.getBounds());

			Class<?> mostSpecificOverridingClass = this.executable.getDeclaringClass();
			for (Class<?> candidate : resolver.getRawTypes(type))
				if (mostSpecificOverridingClass.isAssignableFrom(candidate))
					mostSpecificOverridingClass = candidate;

			Executable override = mostSpecificOverridingClass.equals(executable.getDeclaringClass()) ? executable
					: mostSpecificOverridingClass.getMethod(executable.getName(), executable.getParameterTypes());

			return new InvocableMember<>(resolver, (TypeToken<O>) TypeToken.over(type), override, invocationFunction,
					parameters, variableArityInvocation);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ReflectionException(p -> p.cannotResolveOverride(this, type), e);
		}
	}

	/**
	 * Derive a new instance of {@link InvocableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link InvocableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link InvocableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public <S extends R> InvocableMember<O, S> withTargetType(Class<S> target) {
		return withTargetType(TypeToken.over(target));
	}

	/**
	 * Derive a new instance of {@link InvocableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link InvocableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link InvocableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	@SuppressWarnings("unchecked")
	public <S> InvocableMember<O, S> withTargetType(TypeToken<S> target) {
		if (target == null)
			return (InvocableMember<O, S>) this;

		return (InvocableMember<O, S>) withBoundsFrom(target).withTargetType(target.getType());
	}

	/**
	 * Derive a new instance of {@link InvocableMember} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link InvocableMember} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param target
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @return A new {@link InvocableMember} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public InvocableMember<O, ? extends R> withTargetType(Type target) {
		return withTargetTypeCapture(target);
	}

	@SuppressWarnings("unchecked")
	private <S extends R> InvocableMember<O, S> withTargetTypeCapture(Type target) {
		if (target == null)
			return (InvocableMember<O, S>) this;

		TypeResolver resolver = getResolver();

		ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, returnType.getType(), target, resolver.getBounds());

		return new InvocableMember<>(resolver, ownerType, executable, (BiFunction<O, List<?>, S>) invocationFunction,
				parameters, variableArityInvocation);
	}

	/**
	 * Derive a new {@link InvocableMember} fulfilling two conditions.
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
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @param targetType
	 *          The derived {@link InvocableMember} must be assignment compatible
	 *          with this type.
	 * @param arguments
	 *          The derived {@link InvocableMember} must be loose invocation
	 *          compatible, or failing that variable arity compatible, with the
	 *          given arguments.
	 * @return A new {@link InvocableMember} compatible with the given target type
	 *         and parameters, and which has more specific arguments, type
	 *         arguments, and return type than the receiving
	 *         {@link InvocableMember}.
	 */
	public <U> InvocableMember<O, U> withInferredType(TypeToken<U> targetType, TypeToken<?>... arguments) {
		InvocableMember<O, R> executableMember;
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
	 * Derived a new {@link InvocableMember} instance with generic method
	 * parameters inferred, and if this is a member of a generic type, with
	 * generic type parameters inferred, too.
	 * 
	 * @return The derived {@link InvocableMember} with inferred invocation type.
	 */
	@Override
	public InvocableMember<O, R> infer() {
		TypeResolver resolver = getResolver();

		resolver.infer();

		return new InvocableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * @return A new derived {@link InvocableMember} instance with generic method
	 *         parameters inferred, and if this is a constructor on a generic
	 *         type, with generic type parameters inferred, also.
	 */
	public InvocableMember<O, R> inferParameterTypes() {
		TypeResolver resolver = getResolver();

		for (Type parameter : parameters)
			resolver.infer(parameter);

		return new InvocableMember<>(resolver, ownerType, executable, invocationFunction, parameters,
				variableArityInvocation);
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link InvocableMember} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withStrictApplicability(TypeToken<?>... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link InvocableMember} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withStrictApplicability(List<? extends TypeToken<?>> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link InvocableMember} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withLooseApplicability(TypeToken<?>... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link InvocableMember} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withLooseApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link InvocableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withVariableArityApplicability(TypeToken<?>... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link InvocableMember} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link InvocableMember}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link InvocableMember}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link InvocableMember}.
	 */
	public InvocableMember<O, R> withVariableArityApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(true, arguments);
	}

	private InvocableMember<O, R> withLooseApplicability(boolean variableArity, List<? extends TypeToken<?>> arguments) {
		if (variableArity) {
			if (!executable.isVarArgs())
				throw new ReflectionException(p -> p.invalidVariableArityInvocation(this));

			if (parameters.size() > arguments.size() + 1)
				throw new ReflectionException(p -> p.cannotResolveInvocationType(this, arguments));
		} else if (parameters.size() != arguments.size())
			throw new ReflectionException(p -> p.cannotResolveInvocationType(this, arguments));

		TypeResolver resolver = getResolver();

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

		return new InvocableMember<>(resolver, ownerType, executable, invocationFunction, parameters, variableArity);
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
	 * Derive a new {@link InvocableMember} instance from this, with the given
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
	 *          {@link Executable} wrapped by this {@link InvocableMember}.
	 * @param instantiation
	 *          The type with which to instantiate the given type variable.
	 * @return A new derived {@link InvocableMember} instance with the given
	 *         instantiation substituted for the given type variable.
	 */
	public InvocableMember<O, ? extends R> withTypeArgument(TypeVariable<? extends Executable> variable,
			Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters()).anyMatch(variable::equals)) {
			TypeResolver resolver = this.resolver.copy();
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	/**
	 * Derive a new {@link InvocableMember} instance from this, with the given
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
	 *          {@link Executable} wrapped by this {@link InvocableMember}.
	 * @param variable
	 *          The type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link InvocableMember}.
	 * @param instantiation
	 *          The type with which to instantiate the given type variable.
	 * @return A new derived {@link InvocableMember} instance with the given
	 *         instantiation substituted for the given type variable.
	 */
	@SuppressWarnings("unchecked")
	public <U> InvocableMember<O, ? extends R> withTypeArgument(TypeParameter<U> variable, TypeToken<U> instantiation) {
		return withTypeArgument((TypeVariable<? extends Executable>) variable.getType(), instantiation.getType());
	}

	/**
	 * Derive a new {@link InvocableMember} instance with the given generic type
	 * argument substitutions, as per the behaviour of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          A list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}.
	 * @return A new derived {@link InvocableMember} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order.
	 */
	public InvocableMember<O, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	/**
	 * Derive a new {@link InvocableMember} instance with the given generic type
	 * argument substitutions, as per the behaviour of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          A list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}.
	 * @return A new derived {@link InvocableMember} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order.
	 */
	public InvocableMember<O, ? extends R> withTypeArguments(List<Type> typeArguments) {
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
				throw new ReflectionException(
						p -> p.incompatibleArgument(arguments.get(finalI), parameters.get(finalI), finalI, this));
			}
		return invoke(receiver, arguments);
	}

	/**
	 * Find which constructors can be invoked for this type.
	 * 
	 * @return A list of all {@link Constructor} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances.
	 */
	public static <T> InvocableMemberStream<InvocableMember<Void, T>> getConstructors(TypeToken<T> type) {
		return getConstructorsImpl(type, Class::getConstructors);
	}

	/**
	 * Find which constructors can be invoked for this type.
	 * 
	 * @return A list of all {@link Constructor} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances.
	 */
	public static <T> InvocableMemberStream<InvocableMember<Void, T>> getDeclaredConstructors(TypeToken<T> type) {
		return getConstructorsImpl(type, Class::getDeclaredConstructors);
	}

	@SuppressWarnings("unchecked")
	private static <T> InvocableMemberStream<InvocableMember<Void, T>> getConstructorsImpl(TypeToken<T> type,
			Function<Class<?>, Constructor<?>[]> constructors) {
		return new InvocableMemberStream<>(type,
				stream(constructors.apply(type.getClass())).map(m -> over((Constructor<T>) m, type)));
	}

	/**
	 * find which methods can be invoked on this type, whether statically or on
	 * instances
	 * 
	 * @return a list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances.
	 */
	public static <T> InvocableMemberStream<InvocableMember<? super T, ?>> getMethods(TypeToken<T> type) {
		return getMethodsImpl(type, m -> true, Class::getMethods);
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances, which pass a filter.
	 * 
	 * @param name
	 *          determine which methods may participate
	 * @return a list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances
	 */
	public static <T> InvocableMemberStream<InvocableMember<? super T, ?>> getMethods(TypeToken<T> type, String name) {
		return getMethodsImpl(type, m -> m.getName().equals(name), Class::getMethods);
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances.
	 * 
	 * @return A list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances.
	 */
	public static <T> InvocableMemberStream<InvocableMember<? super T, ?>> getDeclaredMethods(TypeToken<T> type) {
		return getMethodsImpl(type, m -> true, Class::getDeclaredMethods);
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances, which pass a filter.
	 * 
	 * @param name
	 *          determine which methods may participate
	 * @return A list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link InvocableMember} instances.
	 */
	public static <T> InvocableMemberStream<InvocableMember<? super T, ?>> getDeclaredMethods(TypeToken<T> type,
			String name) {
		return getMethodsImpl(type, m -> m.getName().equals(name), Class::getDeclaredMethods);
	}

	private static <T> InvocableMemberStream<InvocableMember<? super T, ?>> getMethodsImpl(TypeToken<T> type,
			Predicate<Method> filter, Function<Class<?>, Method[]> methods) {
		Stream<Method> methodStream = type.getRawTypes().stream().flatMap(t -> Arrays.stream(methods.apply(t)));

		if (type.getRawTypes().stream().allMatch(Types::isInterface))
			methodStream = Stream.concat(methodStream, Arrays.stream(Object.class.getMethods()));

		return new InvocableMemberStream<>(type,
				methodStream.filter(filter).map(m -> (InvocableMember<? super T, ?>) InvocableMember.over(m, type)));
	}

	@SuppressWarnings("unchecked")
	public static <T> InvocableMember<T, ?> findInterfaceMethod(TypeToken<T> type, Consumer<? super T> methodLambda) {
		Method overridden = null;

		for (Class<?> superType : type.getRawTypes()) {
			if (superType.isInterface()) {
				try {
					overridden = Methods.findMethod(superType, (Consumer<Object>) methodLambda);
				} catch (Exception e) {}
			}
		}
		if (overridden == null) {
			throw new ReflectionException(p -> p.cannotFindMethodOn(type.getType()));
		}

		return InvocableMember.over(overridden, type);
	}
}
