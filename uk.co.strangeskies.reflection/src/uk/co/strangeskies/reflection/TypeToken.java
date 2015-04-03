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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * TypeToken is a class providing reflective operations and services over the
 * Java type system. It is analogous to Class<?>, but provides a richer set of
 * tools, and can be used over the domain of all types, not just raw types.
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
	 * Create a TypeToken from a parsed String. Here infinitely recurring types
	 * are represented by, for example:
	 * 
	 * <code>java.util.List<java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable<...>>></code>
	 * 
	 * Where "..." would be substituted, recursively, with the parameterization of
	 * the next-outer instance of the same raw class.
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
	 * The raw type of the type represented by this TypeToken. In the case of
	 * TypeTokens over certain classes of type, for example InferenceVariable or
	 * IntersectionType, this single raw type may be insufficient to properly
	 * describe the type.
	 * 
	 * @return The raw type of the type represented by this TypeToken.
	 */
	public Class<? super T> getRawType() {
		return rawType;
	}

	/**
	 * The raw types of the type represented by this TypeToken. In the case of
	 * most simple TypeTokens, this will be a set with one entry, equal to the
	 * result of {@link #getRawType()}. For more complex types, a raw type may be
	 * derived from each upper bound, of from each item in an intersection type.
	 * 
	 * @return The raw types of the type represented by this TypeToken.
	 */
	public Set<Class<?>> getRawTypes() {
		if (resolver != null
				&& resolver.getBounds().getInferenceVariables().contains(getType()))
			return Types.getRawTypes(IntersectionType.uncheckedFrom(resolver
					.getBounds().getInferenceVariableData().get(getType())
					.getUpperBounds()));
		return Types.getRawTypes(getType());
	}

	/**
	 * This method returns a copy of the Resolver used by this TypeToken.
	 * 
	 * @return
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

	public Type getType() {
		return type;
	}

	public boolean isPrimitive() {
		return Types.isPrimitive(getType());
	}

	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(getType());
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> wrapPrimitive() {
		if (isPrimitive())
			return (TypeToken<T>) of(Types.wrap(getRawType()));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> unwrapPrimitive() {
		if (isPrimitiveWrapper())
			return (TypeToken<T>) of(Types.unwrap(getRawType()));
		else
			return this;
	}

	public boolean isAssignableTo(TypeToken<?> type) {
		return isAssignableTo(type.getType());
	}

	public boolean isAssignableTo(Type type) {
		return Types.isAssignable(getType(), type);
	}

	public boolean isAssignableFrom(TypeToken<?> type) {
		return isAssignableFrom(type.getType());
	}

	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, getType());
	}

	public boolean isContainedBy(TypeToken<?> type) {
		return isContainedBy(type.getType());
	}

	public boolean isContainedBy(Type type) {
		return Types.isContainedBy(type, getType());
	}

	public boolean isContainingOf(TypeToken<?> type) {
		return isContainingOf(type.getType());
	}

	public boolean isContainingOf(Type type) {
		return Types.isContainedBy(getType(), type);
	}

	public Type getComponentType() {
		return Types.getComponentType(getType());
	}

	public Type getTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(getRawType(), type);
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> getTypeArgument(TypeParameter<T> type) {
		return (TypeToken<T>) of(getTypeArgument(type.getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveType(TypeToken<U> type) {
		return (TypeToken<? extends U>) of(getInternalResolver().resolveType(
				type.getType()));
	}

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

	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter,
			TypeToken<V> argument) {
		return (TypeToken<T>) withTypeArgument(parameter.getType(),
				argument.getType());
	}

	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter,
			Class<V> argument) {
		return (TypeToken<T>) withTypeArgument(parameter.getType(), argument);
	}

	private TypeToken<?> withTypeArgument(TypeVariable<?> parameter, Type argument) {
		return of(new TypeSubstitution().where(parameter, argument).resolve(
				getType()));
	}

	@SuppressWarnings("unchecked")
	public Set<? extends Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> Invokable.from((Constructor<T>) m, this))
				.collect(Collectors.toSet());
	}

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

	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<? super T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return resolveConstructorOverload(Arrays.asList(parameters));
	}

	public Invokable<? super T, ? extends T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		Set<? extends Invokable<? super T, ? extends T>> candidates = Invokable
				.resolveApplicableCandidates(getConstructors(), parameters);

		return Invokable.resolveMostSpecificCandidate(candidates);
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		return resolveMethodOverload(name, Arrays.asList(parameters));
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		Set<? extends Invokable<? super T, ? extends Object>> candidates = getMethods(m -> m
				.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		candidates = Invokable.resolveApplicableCandidates(candidates, parameters);

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
