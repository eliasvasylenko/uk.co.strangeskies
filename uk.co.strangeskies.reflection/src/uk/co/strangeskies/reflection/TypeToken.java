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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

/**
 * TypeToken provides reflective operations and services over the Java type
 * system. It is analogous to Class<?>, but provides access to a much richer set
 * of tools, and can be used over the domain of all types, not just raw types.
 * 
 * TypeToken is effectively immutable, though may perform shared caching of
 * results transparently to the user. Like Class, A TypeToken will always be
 * parameterised with the type it reflects over when used as intended.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          This is the type which the TypeToken object references.
 */
public class TypeToken<T> {
	private static ComputingMap<Type, Property<Resolver, Resolver>> RESOLVER_CACHE = new LRUCacheComputingMap<>(
			type -> new IdentityProperty<>(), 200, true);

	private Resolver resolver;

	private final Type type;
	private final Class<? super T> rawType;

	@SuppressWarnings("unchecked")
	TypeToken() {
		if (getClass().getSuperclass().equals(TypeToken.class))
			type = ((ParameterizedType) getClass().getGenericSuperclass())
					.getActualTypeArguments()[0];
		else {
			Resolver resolver = new Resolver();

			Class<?> superClass = getClass();
			Function<Type, InferenceVariable> inferenceVariables = t -> null;
			do {
				resolver.incorporateType(new TypeSubstitution(inferenceVariables)
						.resolve(superClass.getGenericSuperclass()));
				superClass = superClass.getSuperclass();

				final Class<?> finalClass = superClass;
				inferenceVariables = t -> {
					if (t instanceof TypeVariable)
						return resolver.getInferenceVariable(finalClass,
								(TypeVariable<?>) t);
					else
						return null;
				};
			} while (!TypeToken.class.equals(superClass));

			type = resolver
					.resolveTypeVariable(TypeToken.class.getTypeParameters()[0]);
		}

		rawType = (Class<? super T>) Types.getRawType(type);
	}

	@SuppressWarnings("unchecked")
	TypeToken(Type type) {
		this(null, type, (Class<? super T>) Types.getRawType(type));
	}

	TypeToken(Type type, Class<? super T> rawType) {
		this(null, type, rawType);
	}

	TypeToken(Resolver resolver, Type type, Class<? super T> rawType) {
		this.resolver = resolver;
		this.type = type;
		this.rawType = rawType;
	}

	/**
	 * Create a TypeToken for a raw class.
	 * 
	 * @param type
	 *          The class to create a TypeToken for.
	 * @return A TypeToken over the requested class.
	 */
	public static <T> TypeToken<T> of(Class<T> type) {
		return new TypeToken<T>(type, type);
	}

	/**
	 * Create a TypeToken for an arbitrary type, resolved with respect to a given
	 * Resolver.
	 * 
	 * @param resolver
	 *          The resolution context within which to create the class.
	 * @param type
	 *          The requested type.
	 * @return A TypeToken over the requested type.
	 */
	public static TypeToken<?> of(Resolver resolver, Type type) {
		type = resolver.resolveType(type);
		if (resolver.getBounds().getInferenceVariables().contains(type))
			return InferenceTypeToken.of((InferenceVariable) type, new Resolver(
					resolver));
		else
			return of(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type.
	 * 
	 * @param type
	 *          The requested type.
	 * @return A TypeToken over the requested type.
	 */
	public static TypeToken<?> of(Type type) {
		if (type instanceof Class)
			return of((Class<?>) type);
		else if (type instanceof ParameterizedType)
			return ParameterizedTypeToken.of((ParameterizedType) type);
		else if (type instanceof TypeVariable)
			return TypeParameter.of((TypeVariable<?>) type);
		// TODO separate subclasses of TypeToken for these:
		else if (type instanceof WildcardType)
			return new TypeToken<>(type);
		else if (type instanceof GenericArrayType)
			return new TypeToken<>(type);
		else if (type instanceof InferenceVariable)
			return new TypeToken<>(type);
		else if (type instanceof IntersectionType)
			return new TypeToken<>(type);

		throw new IllegalArgumentException("Unexpected type class '"
				+ type.getClass() + "' for type '" + type + "'.");
	}

	/**
	 * Equivalent to the application of {@link TypeToken#of(Type)} to the result
	 * of {@link Types#fromString(String)}.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @return A TypeToken representing the type described by the String.
	 */
	public static TypeToken<?> fromString(String typeString) {
		return of(Types.fromString(typeString));
	}

	protected void validate() {
		getInternalResolver();
	}

	/**
	 * Create a TypeToken over a fresh TypeVariableCapture of the wildcard type
	 * which has the type represented by this TypeToken as an upper bound. For
	 * example, invoking this method on a TypeToken<List<?>> will give a
	 * TypeToken<? extends List<?>>.
	 * 
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? extends T> captureWildcardExtending() {
		return (TypeToken<? extends T>) of(WildcardTypes.upperBounded(getType()));
	}

	/**
	 * Create a TypeToken over a fresh TypeVariableCapture of the wildcard type
	 * which has the type represented by this TypeToken as a lower bound. For
	 * example, invoking this method on a TypeToken<List<?>> will give a
	 * TypeToken<? super List<?>>. TypeVariableCapture.
	 * 
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? super T> captureWildcardSuper() {
		return (TypeToken<? super T>) of(WildcardTypes.lowerBounded(getType()));
	}

	/**
	 * Create a TypeToken over a fresh InferenceVariable with the type represented
	 * by this TypeToken as an upper bound. For example, invoking this method on a
	 * TypeToken<List<?>> will give a TypeToken<? extends List<?>>.
	 * 
	 * @return The resulting TypeToken.
	 */
	@SuppressWarnings("unchecked")
	public InferenceTypeToken<? extends T> inferceExtending() {
		return (InferenceTypeToken<? extends T>) InferenceTypeToken
				.of(WildcardTypes.upperBounded(getType()));
	}

	/**
	 * Create a TypeToken over a fresh InferenceVariable with the type represented
	 * by this TypeToken as a lower bound. For example, invoking this method on a
	 * TypeToken<List<?>> will give a TypeToken<? extends List<?>>.
	 * 
	 * @return The resulting TypeToken.
	 */
	@SuppressWarnings("unchecked")
	public InferenceTypeToken<? super T> inferceSuper() {
		return (InferenceTypeToken<? super T>) InferenceTypeToken.of(WildcardTypes
				.lowerBounded(getType()));
	}

	/**
	 * See {@link Types#getRawType(Type)}.
	 * 
	 * @return The raw type of the type represented by this TypeToken.
	 */
	public Class<? super T> getRawType() {
		return rawType;
	}

	/**
	 * See {@link Types#getRawTypes(Type)}.
	 * 
	 * @return The raw types of the type represented by this TypeToken.
	 */
	public Set<Class<?>> getRawTypes() {
		if (resolver != null
				&& resolver.getBounds().getInferenceVariables().contains(getType()))
			return Types.getRawTypes(IntersectionType.uncheckedFrom(resolver
					.getBounds().getBoundsOn((InferenceVariable) getType())
					.getUpperBounds()));
		return Types.getRawTypes(getType());
	}

	/**
	 * This method returns a copy of the Resolver backing by this TypeToken.
	 * 
	 * @return A new Resolver object containing whichever bounds have been
	 *         internally derived from the type of this TypeToken.
	 */
	public Resolver getResolver() {
		return new Resolver(getInternalResolver());
	}

	protected Resolver getInternalResolver() {
		if (resolver == null) {
			Property<Resolver, Resolver> resolver;
			synchronized (RESOLVER_CACHE) {
				resolver = RESOLVER_CACHE.get(getType());
				if (resolver == null) {
					resolver = RESOLVER_CACHE.putGet(getType());
					resolver.set(new Resolver());
					// TODO move following out of sync block:
					resolver.get().incorporateType(getType());
				}
			}
			this.resolver = resolver.get();
		}

		return resolver;
	}

	@Override
	public String toString() {
		return Types.toString(getType());
	}

	/**
	 * The type described by this TypeToken.
	 * 
	 * @return The actual Type object described.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Is the type a primitive type as per the Java type system.
	 * 
	 * @return True if the type is primitive, false otherwise.
	 */
	public boolean isPrimitive() {
		return Types.isPrimitive(getType());
	}

	/**
	 * Is the type a wrapper for a primitive type as per the Java type system.
	 * 
	 * @return True if the type is a primitive wrapper, false otherwise.
	 */
	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(getType());
	}

	/**
	 * If this TypeToken is a primitive type, determine the wrapped primitive
	 * type.
	 * 
	 * @return The wrapper type of the primitive type this TypeToken represents,
	 *         otherwise this TypeToken itself.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<T> wrapPrimitive() {
		if (isPrimitive())
			return (TypeToken<T>) of(Types.wrapPrimitive(getRawType()));
		else
			return this;
	}

	/**
	 * If this TypeToken is a wrapper of a primitive type, determine the unwrapped
	 * primitive type.
	 * 
	 * @return The primitive type wrapped by this TypeToken, otherwise this
	 *         TypeToken itself.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<T> unwrapPrimitive() {
		if (isPrimitiveWrapper())
			return (TypeToken<T>) of(Types.unwrapPrimitive(getRawType()));
		else
			return this;
	}

	/**
	 * See {@link TypeToken#isAssignableTo(TypeToken)}.
	 * 
	 * @param type
	 *          Forwards to {@code type}.
	 * @return As referenced method.
	 */
	public boolean isAssignableTo(TypeToken<?> type) {
		return isAssignableTo(type.getType());
	}

	/**
	 * Determine if the given type is assignable to this TypeToken, or in other
	 * words, if it is a subtype of this TypeToken.
	 * 
	 * @param type
	 *          The type to which we wish to determine assignability.
	 * @return True if this TypeToken is assignable to the given type, false
	 *         otherwise.
	 */
	public boolean isAssignableTo(Type type) {
		return Types.isAssignable(getType(), type);
	}

	/**
	 * See {@link TypeToken#isAssignableFrom(TypeToken)}.
	 * 
	 * @param type
	 *          Forwards to {@code type}.
	 * @return As referenced method.
	 */
	public boolean isAssignableFrom(TypeToken<?> type) {
		return isAssignableFrom(type.getType());
	}

	/**
	 * Determine if the given type is assignable from this TypeToken, see
	 * {@link Types#isAssignable(Type, Type)}.
	 * 
	 * @param type
	 *          The type from which we wish to determine assignability, as
	 *          {@code from} in {@link Types#isAssignable(Type, Type)}.
	 * @return True if this TypeToken is assignable from the given type, false
	 *         otherwise.
	 */
	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, getType());
	}

	/**
	 * Determine if the given type contains the type described by this TypeToken,
	 * see {@link Types#isContainedBy(Type, Type)}.
	 * 
	 * @param type
	 *          The type within which we are determining containment, as
	 *          {@code from} in {@link Types#isContainedBy(Type, Type)}.
	 * @return True if the type passed as an argument <em>contains</em> this
	 *         TypeToken, false otherwise.
	 */
	public boolean isContainedBy(TypeToken<?> type) {
		return isContainedBy(type.getType());
	}

	/**
	 * Determine if the given type contains the type described by this TypeToken.
	 * 
	 * In other words, if either this type or the given type are wildcards,
	 * determine if every possible instantiation of this TypeToken is also a valid
	 * instantiation of the given type. Or, if neither type is a wildcard,
	 * determine whether both types are assignable to each other.
	 * 
	 * @param type
	 *          The type within which we are determining containment.
	 * @return True if the type passed as an argument <em>contains</em> this
	 *         TypeToken, false otherwise.
	 */
	public boolean isContainedBy(Type type) {
		return Types.isContainedBy(type, getType());
	}

	/**
	 * Determine if the given type is contained by the type described by this
	 * TypeToken.
	 * 
	 * In other words, if either this type or the given type are wildcards,
	 * determine if every possible instantiation of the given type is also a valid
	 * instantiation of this TypeToken. Or, if neither type is a wildcard,
	 * determine whether both types are assignable to each other.
	 * 
	 * @param type
	 *          The type which we are determining the containment of.
	 * @return True if the type passed as an argument <em>is contained by</em>
	 *         this TypeToken, false otherwise.
	 */
	public boolean isContainingOf(TypeToken<?> type) {
		return isContainingOf(type.getType());
	}

	/**
	 * Determine if the given type is contained by the type described by this
	 * TypeToken.
	 * 
	 * In other words, if either this type or the given type are wildcards,
	 * determine if every possible instantiation of the given type is also a valid
	 * instantiation of this TypeToken. Or, if neither type is a wildcard,
	 * determine whether both types are assignable to each other.
	 * 
	 * @param type
	 *          The type which we are determining the containment of.
	 * @return True if the type passed as an argument <em>is contained by</em>
	 *         this TypeToken, false otherwise.
	 */
	public boolean isContainingOf(Type type) {
		return Types.isContainedBy(getType(), type);
	}

	/**
	 * See {@link TypeToken#getTypeArgument(TypeParameter)}.
	 * 
	 * @param type
	 *          The type variable for which we wish to determine an instantiation.
	 * @return The type instantiating the given type parameter, if such an
	 *         instantiation exists, otherwise the given parameter itself.
	 */
	public Type getTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(getRawType(), type);
	}

	/**
	 * Try to find an instantiation of the given type variable within the context
	 * provided by the type hierarchy of the type described by this TypeToken.
	 * 
	 * @param type
	 *          The type variable for which we wish to determine an instantiation.
	 * @return A TypeToken over the instantiation of the given type parameter, if
	 *         one exists, otherwise a TypeToken over the given parameter.
	 */
	@SuppressWarnings("unchecked")
	public <U> TypeToken<U> getTypeArgument(TypeParameter<U> type) {
		return (TypeToken<U>) of(getTypeArgument(type.getType()));
	}

	/**
	 * Resolve a given type with respect to the type represented by this
	 * TypeToken. This means that any occurrences of TypeVariable or
	 * InferenceVariable objects will be substituted by any instantiations thereof
	 * which may be present as part of this TypeToken's type hierarchy.
	 * 
	 * @param type
	 *          The type we wish to resolve.
	 * @return A new TypeToken with all type variables and inference variables
	 *         which have instantiations resolved.
	 */
	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveType(TypeToken<U> type) {
		return (TypeToken<? extends U>) of(getInternalResolver().resolveType(
				type.getType()));
	}

	/**
	 * For a given generic superclass of this type, determine the type arguments
	 * of the exact supertype.
	 * 
	 * @param superclass
	 *          The class of the supertype parameterization we wish to determine.
	 * @return A TypeToken over the supertype of the requested class.
	 */
	@SuppressWarnings("unchecked")
	public <U> ParameterizedTypeToken<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (!ParameterizedTypes.isGeneric(superclass))
			throw new IllegalArgumentException();

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(superclass,
				new HashMap<>());

		getInternalResolver().resolveTypeHierarchy(getType(), superclass);

		return (ParameterizedTypeToken<? extends U>) ParameterizedTypeToken
				.of((ParameterizedType) getInternalResolver().resolveType(superclass,
						parameterizedType));
	}

	/**
	 * Attempt to determine the type arguments with which a subtype of a given
	 * class would be parameterized such that it be a valid subtype. This may not
	 * always be possible, but for certain subtype relations it will, based on the
	 * reduction and incorporation rules of the Java type inference algorithm.
	 * 
	 * @param subclass
	 *          The class of the subtype parameterization we wish to determine.
	 * @return A TypeToken over the best effort parameterization of the requested
	 *         class such that it be a subtype.
	 */
	@SuppressWarnings("unchecked")
	public <U> ParameterizedTypeToken<? extends U> resolveSubtypeParameters(
			Class<U> subclass) {
		if (!ParameterizedTypes.isGeneric(subclass))
			throw new IllegalArgumentException();

		ParameterizedType parameterizedType = (ParameterizedType) ParameterizedTypes
				.uncheckedFrom(subclass, new HashMap<>());

		getInternalResolver().resolveTypeHierarchy(parameterizedType, getRawType());

		return (ParameterizedTypeToken<? extends U>) ParameterizedTypeToken
				.of((ParameterizedType) getInternalResolver().resolveType(subclass,
						parameterizedType));
	}

	/**
	 * <p>
	 * Substitute all occurrences of a TypeParameter instance mentioned by this
	 * type with a specific type satisfying the bounds of that parameter.
	 * 
	 * As an example, the following method could be used to derive instances of
	 * TypeToken over different parameterizations of List<?> at runtime.
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * {@code public List<T> getList(TypeToken<T> clazz)} {
	 * 	{@code return new TypeLiteral<T>()} {}.withTypeArgument(new {@code TypeParameter<T>()} {}, clazz);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param parameter
	 *          The parameter to make a substitution for.
	 * @param argument
	 *          The argument to substitute for that parameter.
	 * @return A new TypeToken instance over the type resulting from the
	 *         substitution.
	 */
	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter,
			TypeToken<V> argument) {
		return (TypeToken<T>) withTypeArgument(parameter.getType(),
				argument.getType());
	}

	/**
	 * As with {@link TypeToken#withTypeArgument(TypeParameter, TypeToken)}.
	 * 
	 * @param parameter
	 *          The parameter to make a substitution for.
	 * @param argument
	 *          The argument to substitute for that parameter.
	 * @return A new TypeToken instance over the type resulting from the
	 *         substitution.
	 */
	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter,
			Class<V> argument) {
		return (TypeToken<T>) withTypeArgument(parameter.getType(), argument);
	}

	private TypeToken<?> withTypeArgument(TypeVariable<?> parameter, Type argument) {
		return of(new TypeSubstitution().where(parameter, argument).resolve(
				getType()));
	}

	/**
	 * Find which constructors can be invoked for this type.
	 * 
	 * @return A list of all {@link Constructor} objects applicable to this type,
	 *         wrapped in {@link Invokable} instances.
	 */
	@SuppressWarnings("unchecked")
	public Set<? extends Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> Invokable.from((Constructor<T>) m, this))
				.collect(Collectors.toSet());
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances.
	 * 
	 * @return A list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link Invokable} instances.
	 */
	public Set<? extends Invokable<? super T, ?>> getMethods() {
		return getMethods(m -> true);
	}

	private Set<? extends Invokable<? super T, ?>> getMethods(
			Predicate<Method> filter) {
		Stream<Method> methodStream = getRawTypes().stream().flatMap(
				t -> Arrays.stream(t.getMethods()));

		if (getRawTypes().stream().allMatch(Types::isInterface))
			methodStream = Stream.concat(methodStream,
					Arrays.stream(Object.class.getMethods()));

		return methodStream.filter(filter)
				.map(m -> Invokable.from(m, this, of(m.getGenericReturnType())))
				.collect(Collectors.toSet());
	}

	/**
	 * Find which methods and constructors can be invoked on this type, whether
	 * statically or on instances.
	 * 
	 * @return A list of all {@link Method} and {@link Constructor} objects
	 *         applicable to this type, wrapped in {@link Invokable} instances.
	 */
	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	/**
	 * Resolve which constructor invocation matches the given name and argument
	 * list, according to the Java 8 method overload resolution rules.
	 * 
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ? extends T> resolveConstructorOverload(
			Type... arguments) {
		return resolveConstructorOverload(Arrays.asList(arguments));
	}

	/**
	 * Resolve which constructor invocation matches the given name and argument
	 * list, according to the Java 8 method overload resolution rules.
	 * 
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ? extends T> resolveConstructorOverload(
			List<? extends Type> arguments) {
		Set<? extends Invokable<? super T, ? extends T>> candidates = Invokable
				.resolveApplicableCandidates(getConstructors(), arguments);

		return Invokable.resolveMostSpecificCandidate(candidates);
	}

	/**
	 * Resolve which method invocation matches the given name and argument list,
	 * according to the Java 8 method overload resolution rules.
	 * 
	 * @param name
	 *          The name of the method.
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type... arguments) {
		return resolveMethodOverload(name, Arrays.asList(arguments));
	}

	/**
	 * Resolve which method invocation matches the given name and argument list,
	 * according to the Java 8 method overload resolution rules.
	 * 
	 * @param name
	 *          The name of the method.
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> arguments) {
		Set<? extends Invokable<? super T, ? extends Object>> candidates = getMethods(m -> m
				.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		candidates = Invokable.resolveApplicableCandidates(candidates, arguments);

		return Invokable.resolveMostSpecificCandidate(candidates);
	}

	/**
	 * This method will attempt to substitute any inference variables mentioned by
	 * this type with their instantiations, if instantiations are available, and
	 * return a TypeToken over the resulting type.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<T> resolve() {
		return (TypeToken<T>) TypeToken.of(getInternalResolver(), getType());
	}

	/**
	 * This method will attempt to infer the actual type represented by this
	 * TypeToken, which means the types of any inference variables mentioned will
	 * be inferred and substituted. The receiver TypeToken instance will not be
	 * changed.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<T> infer() {
		Resolver resolver = getResolver();
		resolver.infer(resolver.getBounds().getInferenceVariablesMentionedBy(
				getType()));
		return (TypeToken<T>) TypeToken.of(resolver, getType());
	}
}
