/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
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
package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Types.getRawType;
import static uk.co.strangeskies.reflection.token.ExecutableTokenQuery.executableQuery;
import static uk.co.strangeskies.reflection.token.TypeParameter.forTypeVariable;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.streamOptional;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.tryOptional;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.zip;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.utilities.collection.StreamUtilities;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link ExecutableToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the receiver type of the executable
 * @param <R>
 *          the return type of the executable
 */
public class ExecutableToken<O, R> implements MemberToken<O, ExecutableToken<O, R>> {
	private final Executable executable;
	private final List<Type> typeArguments;

	private final TypeToken<? super O> receiverType;
	private final TypeToken<? extends R> returnType;
	private final List<ExecutableParameter> parameters;

	private final boolean variableArityInvocation;

	protected ExecutableToken(Class<?> instance, Constructor<?> constructor) {
		this(instance, constructor.getDeclaringClass(), constructor);
	}

	protected ExecutableToken(Class<?> instance, Method method) {
		this(instance, method.getReturnType(), method);
	}

	@SuppressWarnings("unchecked")
	private ExecutableToken(Class<?> receiverType, Class<?> returnType, Executable executable) {
		this.executable = executable;
		this.typeArguments = null;

		this.receiverType = (TypeToken<? super O>) forClass(receiverType);
		this.returnType = (TypeToken<? extends R>) forClass(returnType);
		this.parameters = Arrays
				.stream(executable.getParameters())
				.map(p -> new ExecutableParameter(p, p.getType()))
				.collect(toList());

		this.variableArityInvocation = false;
	}

	/*
	 * To avoid unnecessary recalculation, or a huge list of constructors, the
	 * receiver type, return type, type arguments etc. are assumed to be in a
	 * consistent state at this point. The responsibility to ensure this is with
	 * the caller.
	 */
	protected ExecutableToken(
			TypeToken<? super O> receiverType,
			TypeToken<? extends R> returnType,
			List<ExecutableParameter> parameters,
			List<Type> typeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		this.executable = executable;
		this.variableArityInvocation = variableArityInvocation;
		this.typeArguments = typeArguments;
		this.receiverType = receiverType;
		this.returnType = returnType;
		this.parameters = parameters;
	}

	private <P, S> ExecutableToken<P, S> withExecutableTokenData(
			TypeToken<? super P> receiverType,
			TypeToken<? extends S> returnType,
			List<ExecutableParameter> parameters,
			List<Type> typeArguments,
			Executable executable,
			boolean variableArityInvocation) {
		return new ExecutableToken<>(
				receiverType,
				returnType,
				parameters,
				typeArguments,
				executable,
				variableArityInvocation);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor} of an outer or static class.
	 * 
	 * <p>
	 * If the method is generic it will be parameterized with its own type
	 * variables.
	 * 
	 * @param constructor
	 *          the constructor to wrap
	 * @return an executable member wrapping the given constructor
	 */
	public static ExecutableToken<Void, ?> overConstructor(Constructor<?> constructor) {
		if (!Modifier.isStatic(constructor.getDeclaringClass().getModifiers())) {
			throw new ReflectionException(m -> m.declaringClassMustBeStatic(constructor));
		}
		return new ExecutableToken<>(void.class, constructor);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor} of an inner class.
	 * 
	 * <p>
	 * If the constructor or the enclosing class are generic they will be
	 * parameterized with their own type variables.
	 * 
	 * @param constructor
	 *          the constructor to wrap
	 * @return an executable member wrapping the given method
	 */
	public static ExecutableToken<?, ?> overInnerConstructor(Constructor<?> constructor) {
		return new ExecutableToken<>(constructor.getDeclaringClass().getEnclosingClass(), constructor);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a static
	 * {@link Method}.
	 * 
	 * <p>
	 * If the method is generic it will be parameterized with its own type
	 * variables.
	 * 
	 * @param method
	 *          the method to wrap
	 * @return an executable member wrapping the given method
	 */
	public static ExecutableToken<Void, ?> overStaticMethod(Method method) {
		if (!Modifier.isStatic(method.getModifiers())) {
			throw new ReflectionException(m -> m.methodMustBeStatic(method));
		}
		return new ExecutableToken<>(void.class, method);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to an
	 * instance {@link Method}.
	 * 
	 * <p>
	 * If the method or its declaring class are generic they will be parameterized
	 * with their own type variables.
	 * 
	 * @param method
	 *          the method to wrap
	 * @return an executable member wrapping the given method
	 */
	public static ExecutableToken<?, ?> overMethod(Method method) {
		return new ExecutableToken<>(method.getDeclaringClass(), method);
	}

	/**
	 * If the invocation is raw, the target type and method are parameterized with
	 * inference variables. Bounds are incorporated according to those present on
	 * the type variables each argument instantiates.
	 * 
	 * <p>
	 * If the invocation is already parameterized, the existing arguments are
	 * substituted according to their type. Bounds are incorporated according to
	 * those present on the type variables each argument instantiates.
	 * 
	 * <ul>
	 * <li>Substitute wildcards with inference variables, incorporating bounds
	 * according to those present on the wildcard.</li>
	 * 
	 * <li>Do not substitute types which are not wildcards.</li>
	 * </ul>
	 * 
	 * @return an inference over the exact invocation type
	 */
	public ExecutableToken<? extends O, R> infer() {
		if (!isGeneric()) {
			return this;
		} else if (isRaw()) {
			Collection<TypeArgument<?>> inferenceVariables = new TypeResolver()
					.inferTypeParameters(getMember())
					.map(e -> forTypeVariable(e.getKey()).asType(e.getValue()))
					.collect(toList());
			Map<TypeVariable<?>, Type> inferenceVariableMap = new HashMap<>();

			// TODO

			return withExecutableTokenData(
					getReceiverType().withTypeArguments(inferenceVariables),
					getReturnType().withTypeArguments(inferenceVariables),
					Arrays
							.stream(getMember().getParameters())
							.map(p -> new ExecutableParameter(p, p.getParameterizedType()))
							.collect(toList()),
					asList(getMember().getTypeParameters()),
					getMember(),
					isVariableArityInvocation());
		} else {
			Collection<TypeArgument<?>> inferenceVariables = new TypeResolver()
					.inferTypeParameters(getMember())
					.map(e -> forTypeVariable(e.getKey()).asType(e.getValue()))
					.collect(toList());

			return this;
		}
	}

	/**
	 * If the executable represents a raw invocation, parameterize it with its own
	 * type parameters, otherwise return the executable itself.
	 * 
	 * @return the parameterized version of the executable where applicable, else
	 *         the executable
	 */
	public ExecutableToken<? extends O, R> parameterize() {
		if (isRaw()) {
			@SuppressWarnings("unchecked")
			TypeToken<? extends R> returnType = isConstructor() ? getReturnType().parameterize()
					: (TypeToken<? extends R>) forType(((Method) getMember()).getGenericReturnType());

			return withExecutableTokenData(
					getReceiverType().parameterize(),
					returnType,
					Arrays
							.stream(getMember().getParameters())
							.map(p -> new ExecutableParameter(p, p.getParameterizedType()))
							.collect(toList()),
					asList(getMember().getTypeParameters()),
					getMember(),
					isVariableArityInvocation());
		} else {
			return this;
		}
	}

	@Override
	public Executable getMember() {
		return executable;
	}

	@Override
	public Optional<TypeToken<?>> getOwningDeclaration() {
		return Optional.of(isConstructor() ? getReturnType() : getReceiverType());
	}

	/**
	 * @return the name of the executable member
	 */
	@Override
	public String getName() {
		return getMember().getName();
	}

	@Override
	public BoundSet getBounds() {
		return receiverType.getBounds();
	}

	@Override
	public String toString() {
		return toString(parameters);
	}

	private String toString(List<ExecutableParameter> parameters) {
		StringBuilder builder = new StringBuilder();

		getVisibility().getKeyword().ifPresent(visibility -> builder.append(visibility).append(" "));

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
					.append(getTypeArguments().map(TypeArgument::getType).map(Objects::toString).collect(joining(", ")))
					.append("> ");
		}

		builder.append(returnType).toString();
		if (getMember() instanceof Method)
			builder.append(" ").append(receiverType).append(".").append(getMember().getName());

		return builder
				.append("(")
				.append(parameters.stream().map(Objects::toString).collect(joining(", ")))
				.append(")")
				.toString();
	}

	/**
	 * @return true if the wrapped executable is abstract, false otherwise
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(getMember().getModifiers());
	}

	/**
	 * @return true if the wrapped executable is native, false otherwise
	 */
	public boolean isNative() {
		return Modifier.isNative(getMember().getModifiers());
	}

	/**
	 * @return true if the executable is a constructor, false otherwise
	 */
	public boolean isConstructor() {
		return getMember() instanceof Constructor<?>;
	}

	/**
	 * @return true if the executable is a method, false otherwise
	 */
	public boolean isMethod() {
		return getMember() instanceof Method;
	}

	/**
	 * @return true if the wrapped executable is strict, false otherwise
	 */
	public boolean isStrict() {
		return Modifier.isStrict(getMember().getModifiers());
	}

	/**
	 * @return true if the wrapped executable is synchronized, false otherwise
	 */
	public boolean isSynchronized() {
		return Modifier.isSynchronized(getMember().getModifiers());
	}

	/**
	 * @return true if the wrapped executable is generic, false otherwise
	 */
	public boolean isGeneric() {
		return getMember().getTypeParameters().length > 0;
	}

	/**
	 * @return true if the wrapped executable is variable arity, false otherwise
	 */
	public boolean isVariableArityDefinition() {
		return getMember().isVarArgs();
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
	 * @return copy of the {@link ExecutableToken} flagged to be invoked with
	 *         {@link #isVariableArityInvocation() variable arity}
	 */
	public ExecutableToken<O, R> asVariableArityInvocation() {
		if (isVariableArityInvocation()) {
			return this;
		} else if (!isVariableArityDefinition()) {
			throw new ReflectionException(p -> p.invalidVariableArityInvocation(getMember()));
		} else {
			return withExecutableTokenData(receiverType, returnType, parameters, typeArguments, executable, true);
		}
	}

	@Override
	public TypeToken<? super O> getReceiverType() {
		return receiverType;
	}

	/**
	 * @return The exact return type of this executable member instance. Generic
	 *         type parameters may include inference variables.
	 */
	public TypeToken<? extends R> getReturnType() {
		return returnType;
	}

	/**
	 * @return The exact types of the expected parameters of this executable
	 *         member instance. Inference variables may be mentioned.
	 */
	public Stream<ExecutableParameter> getParameters() {
		return parameters.stream();
	}

	/**
	 * Derive a new {@link ExecutableToken} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link ExecutableToken} will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @return The newly derived {@link ExecutableToken}.
	 */
	@Override
	public ExecutableToken<O, R> withBounds(BoundSet bounds) {
		if (isRaw() || bounds.isEmpty()) {
			return this;
		} else {
			return withTypeSubstitution(getBounds().withBounds(bounds), new TypeSubstitution());
		}
	}

	@Override
	public ExecutableToken<O, R> withReceiverType(Type type) {
		return withBounds(receiverType.withConstraintFrom(Kind.SUBTYPE, type).getBounds());
	}

	@SuppressWarnings("unchecked")
	public <U> ExecutableToken<U, R> getOverride(TypeToken<U> type) {
		boolean matchingRawType = (type.getType() instanceof Class<?> || type.getType() instanceof ParameterizedType)
				&& type.getRawType().equals(receiverType.getRawType());

		if (matchingRawType) {
			return (ExecutableToken<U, R>) withReceiverType(type);
		} else if (isConstructor()) {
			throw new ReflectionException(m -> m.cannotOverrideConstructor(getMember(), type.getType()));
		}

		/*
		 * If there is a public override we can find out via getMethod, as even if
		 * the erased signature is different there will be a bridge method with the
		 * same erased signature.
		 * 
		 * TODO the bridge method may exist for another reason and not be a true
		 * override. Should we return the bridge in this case? What if the bridge
		 * somehow *hides* an actual override?
		 */
		Method override = type
				.getUpperBounds()
				.flatMap(
						t -> streamOptional(tryOptional(() -> getRawType(t).getMethod(getName(), getMember().getParameterTypes()))))
				.findFirst()
				.orElse(null);

		if (override == null) {
			/*
			 * Either there is no override, or there is a non-public override, so we
			 * have to check the declared methods on each superclass. Again, if the
			 * erased signature is different we will still find a bridge.
			 */
			override = StreamUtilities
					.<Class<?>>iterate(type.getRawType(), Class::getSuperclass)
					.flatMap(
							t -> streamOptional(
									tryOptional(() -> getRawType(t).getDeclaredMethod(getName(), getMember().getParameterTypes()))))
					.findFirst()
					.orElse(null);
		}

		if (override == null) {
			return (ExecutableToken<U, R>) withReceiverType(type);
		}

		if (override.isBridge()) {
			/*
			 * TODO find which method the bridge is to.
			 */
		} else {
			/*
			 * TODO this is the exact override
			 */
		}

		type.withConstraintTo(SUBTYPE, receiverType);

		return null;
	}

	public Stream<ExecutableToken<O, ? super R>> getOverridden() {
		if (isConstructor()) {
			return Stream.empty();
		} else {
			Class<?>[] erasedParameters = getParameters().map(ExecutableParameter::getErasure).toArray(Class<?>[]::new);

			return Arrays
					.stream(getMember().getDeclaringClass().getMethods())
					.filter(m -> m.getName().equals(getName()) && m.getParameterTypes().equals(erasedParameters))
					.map(
							m -> withExecutableTokenData(
									receiverType,
									returnType,
									parameters,
									typeArguments,
									m,
									variableArityInvocation));
		}
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public <S extends R> ExecutableToken<O, S> withTargetType(Class<S> target) {
		return withTargetType(TypeToken.forClass(target));
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	@SuppressWarnings("unchecked")
	public <S> ExecutableToken<O, S> withTargetType(TypeToken<S> target) {
		if (target == null)
			return (ExecutableToken<O, S>) this;

		return (ExecutableToken<O, S>) withBounds(target.getBounds()).withTargetType(target.getType());
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public ExecutableToken<O, R> withTargetType(Type target) {
		if (target == null)
			return this;

		return withBounds(
				new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, returnType.getType(), target).reduce(getBounds()));
	}

	/**
	 * Derived a new {@link ExecutableToken} instance with generic method
	 * parameters inferred, and if this is a member of a generic type, with
	 * generic type parameters inferred, too.
	 * 
	 * @return The derived {@link ExecutableToken} with inferred invocation type.
	 */
	@Override
	public ExecutableToken<O, R> resolve() {
		TypeResolver resolver = new TypeResolver(getBounds());
		resolver.resolve();
		return withBounds(resolver.getBounds());
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withStrictApplicability(TypeToken<?>... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withStrictApplicability(List<? extends TypeToken<?>> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withLooseApplicability(TypeToken<?>... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withLooseApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link ExecutableToken}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withVariableArityApplicability(TypeToken<?>... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link ExecutableToken}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withVariableArityApplicability(List<? extends TypeToken<?>> arguments) {
		return asVariableArityInvocation().withLooseApplicability(true, arguments);
	}

	private ExecutableToken<O, R> withLooseApplicability(boolean variableArity, List<? extends TypeToken<?>> arguments) {
		if (variableArity) {
			if (parameters.size() > arguments.size() + 1) {
				throw new ReflectionException(
						p -> p.cannotResolveInvocationType(
								getMember(),
								arguments.stream().map(TypeToken::getType).collect(toList())));
			}
		} else if (parameters.size() != arguments.size()) {
			throw new ReflectionException(
					p -> p
							.cannotResolveInvocationType(getMember(), arguments.stream().map(TypeToken::getType).collect(toList())));
		}

		TypeResolver resolver = new TypeResolver(getBounds());

		if (!parameters.isEmpty()) {
			Iterator<ExecutableParameter> parameters = this.parameters.iterator();
			Type nextParameter = parameters.next().getType();
			Type parameter = nextParameter;
			for (TypeToken<?> argument : arguments) {
				if (nextParameter != null) {
					parameter = nextParameter;
					if (parameters.hasNext()) {
						nextParameter = parameters.next().getType();
					} else if (variableArity) {
						parameter = Types.getComponentType(parameter);
						nextParameter = null;
					}
				}

				resolver.incorporateBounds(argument.getBounds());
				resolver.reduceConstraint(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, argument.getType(), parameter));
			}

			// Test resolution is possible.
			resolver.copy().resolve();
		}

		return withBounds(resolver.getBounds());
	}

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	@Override
	public Stream<TypeParameter<?>> getTypeParameters() {
		return stream(getMember().getTypeParameters()).map(TypeParameter::forTypeVariable);
	}

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	@Override
	public Stream<TypeArgument<?>> getTypeArguments() {
		return zip(getTypeParameters(), typeArguments.stream(), TypeParameter::asType);
	}

	@Override
	public int getTypeParameterCount() {
		return getMember().getTypeParameters().length;
	}

	@Override
	public ExecutableToken<O, R> withTypeArguments(Collection<? extends TypeArgument<?>> arguments) {
		if (arguments.isEmpty()) {
			return this;
		}

		Map<TypeVariable<?>, Type> argumentTypes = getAllTypeArguments()
				.collect(toMap(TypeArgument::getParameter, TypeArgument::getType));

		Map<Type, Type> argumentSubstitutions = new HashMap<>();
		Map<InferenceVariable, Type> inferenceVariableInstantiations = new HashMap<>();
		for (TypeArgument<?> argument : arguments) {
			TypeVariable<?> parameter = argument.getParameter();
			Type type = argument.getType();

			Type previousType = argumentTypes.put(parameter, type);

			if (previousType == null) {
				throw new ReflectionException(m -> m.cannotParameterizeOnDeclaration(parameter, getMember()));
			} else if (previousType.equals(parameter)) {
				argumentSubstitutions.put(parameter, type);
			} else if (previousType instanceof InferenceVariable) {
				argumentSubstitutions.put(previousType, type);
				inferenceVariableInstantiations.put((InferenceVariable) previousType, type);
			} else if (!previousType.equals(type)) {
				throw new ReflectionException(m -> m.cannotParameterizeWithReplacement(type, previousType));
			}
		}

		BoundSet bounds = getBounds().withInstantiations(inferenceVariableInstantiations);
		TypeSubstitution typeSubstitution = new TypeSubstitution(argumentSubstitutions);

		typeSubstitution = typeSubstitution.where(
				bounds::containsInferenceVariable,
				t -> bounds.getBoundsOn((InferenceVariable) t).getInstantiation().orElse(null));

		return withTypeSubstitution(bounds, typeSubstitution);
	}

	protected ExecutableToken<O, R> withTypeSubstitution(BoundSet bounds, TypeSubstitution typeSubstitution) {
		return withExecutableTokenData(
				determineReceiverType(bounds, typeSubstitution),
				determineReturnType(bounds, typeSubstitution),
				determineParameterTypes(typeSubstitution),
				determineTypeArguments(typeSubstitution),
				executable,
				variableArityInvocation);
	}

	private TypeToken<? super O> determineReceiverType(BoundSet bounds, TypeSubstitution typeArguments) {
		if (getReceiverType().getType() instanceof Class<?>) {
			return getReceiverType();
		} else {
			return new TypeToken<>(bounds, typeArguments.resolve(getReceiverType().getType()));
		}
	}

	private TypeToken<? extends R> determineReturnType(BoundSet bounds, TypeSubstitution typeArguments) {
		if (getReturnType().getType() instanceof Class<?>) {
			return getReturnType();
		} else {
			return new TypeToken<>(bounds, typeArguments.resolve(getReturnType().getType()));
		}
	}

	private List<ExecutableParameter> determineParameterTypes(TypeSubstitution typeSubstitution) {
		return getParameters()
				.map(p -> new ExecutableParameter(p.getParameter(), p.getParameter().getParameterizedType()))
				.collect(toList());
	}

	private List<Type> determineTypeArguments(TypeSubstitution typeSubstitution) {
		return isRaw() ? null : typeArguments.stream().map(typeSubstitution::resolve).collect(toList());
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
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invoke(O receiver, Object... arguments) {
		try {
			if (variableArityInvocation) {
				int regularArgumentCount = parameters.size() - 1;

				Object[] actualArguments = new Object[parameters.size()];
				Object[] varargs = (Object[]) Array.newInstance(
						Types.getRawType(parameters.get(regularArgumentCount).getType()).getComponentType(),
						arguments.length - regularArgumentCount);

				System.arraycopy(arguments, 0, actualArguments, 0, regularArgumentCount);
				actualArguments[actualArguments.length - 1] = varargs;

				System.arraycopy(arguments, 0, varargs, regularArgumentCount, arguments.length - regularArgumentCount);

				return invokeImpl(receiver, actualArguments);
			} else {
				return invokeImpl(receiver, arguments);
			}
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new ReflectionException(p -> p.invocationFailed(getMember(), receiverType.getType(), arguments), e);
		}
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
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invoke(O receiver, List<? extends Object> arguments) {
		return invoke(receiver, arguments.toArray());
	}

	@SuppressWarnings("unchecked")
	protected R invokeImpl(O receiver, Object[] arguments)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (isConstructor()) {
			if (!getReceiverType().getType().equals(void.class)) {
				Object[] argumentsWithReceiver = new Object[arguments.length + 1];
				argumentsWithReceiver[0] = receiver;
				System.arraycopy(arguments, 0, argumentsWithReceiver, 1, arguments.length);
				arguments = argumentsWithReceiver;
			}

			return (R) ((Constructor<?>) getMember()).newInstance(arguments);
		} else {
			return (R) ((Method) getMember()).invoke(receiver, arguments);
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
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the typed argument list for the invocation
	 * @return the result of the invocation
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
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the typed argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invokeSafely(O receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getTypeToken().satisfiesConstraintTo(LOOSE_COMPATIBILILTY, parameters.get(i).getType())) {
				int finalI = i;
				throw new ReflectionException(
						p -> p.incompatibleArgument(
								arguments.get(finalI).getObject(),
								arguments.get(finalI).getTypeToken().getType(),
								parameters.get(finalI).getType(),
								finalI,
								getMember()));
			}
		return invoke(receiver, arguments);
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances.
	 * 
	 * @param declaringClass
	 *          the declaring class for which to retrieve the methods
	 * @return all {@link Method} objects applicable to this type, wrapped in
	 *         {@link ExecutableToken} instances
	 */
	public static ExecutableTokenQuery<ExecutableToken<Void, ?>, ?> staticMethods(Class<?> declaringClass) {
		Stream<Method> methodStream = stream(declaringClass.getDeclaredMethods())
				.filter(m -> Modifier.isStatic(m.getModifiers()));

		return executableQuery(methodStream, ExecutableToken::overStaticMethod);
	}
}
