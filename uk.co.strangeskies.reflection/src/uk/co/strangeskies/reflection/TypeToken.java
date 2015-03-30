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

	public static <T> TypeToken<T> of(Class<T> type) {
		return new TypeToken<T>(type, type);
	}

	public static TypeToken<?> of(Resolver resolver, Type type) {
		type = resolver.resolveType(type);
		System.out.println(resolver.getBounds());
		if (resolver.getBounds().getInferenceVariables().contains(type))
			return InferenceTypeToken.of((InferenceVariable) type, new Resolver(
					resolver));
		else
			return of(type);
	}

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

		throw new IllegalArgumentException("Unexpected type class '"
				+ type.getClass() + "' for type '" + type + "'.");
	}

	public static TypeToken<?> fromString(String typeString) {
		return of(Types.fromString(typeString));
	}

	protected void validate() {
		getInternalResolver();
	}

	public boolean isAbstract() {
		return Types.isAbstract(getRawType());
	}

	public boolean isFinal() {
		return Types.isFinal(getRawType());
	}

	public boolean isInterface() {
		return Types.isInterface(getRawType());
	}

	public boolean isPrivate() {
		return Types.isPrivate(getRawType());
	}

	public boolean isProtected() {
		return Types.isProtected(getRawType());
	}

	public boolean isPublic() {
		return Types.isPublic(getRawType());
	}

	public boolean isStatic() {
		return Types.isStatic(getRawType());
	}

	public boolean isWildcard() {
		return getType() instanceof WildcardType;
	}

	@SuppressWarnings("unchecked")
	public TypeToken<? extends T> captureWildcardExtending() {
		return (TypeToken<? extends T>) of(WildcardTypes.upperBounded(getType()));
	}

	@SuppressWarnings("unchecked")
	public TypeToken<? super T> captureWildcardSuper() {
		return (TypeToken<? super T>) of(WildcardTypes.lowerBounded(getType()));
	}

	@SuppressWarnings("unchecked")
	public InferenceTypeToken<? extends T> inferceExtending() {
		return (InferenceTypeToken<? extends T>) InferenceTypeToken
				.of(WildcardTypes.upperBounded(getType()));
	}

	@SuppressWarnings("unchecked")
	public InferenceTypeToken<? super T> inferceSuper() {
		return (InferenceTypeToken<? super T>) InferenceTypeToken.of(WildcardTypes
				.lowerBounded(getType()));
	}

	public Class<?> getNonStaticallyEnclosingClass() {
		return Types.getNonStaticallyEnclosingClass(getRawType());
	}

	public Class<? super T> getRawType() {
		return rawType;
	}

	public Set<Class<?>> getRawTypes() {
		if (resolver != null
				&& resolver.getBounds().getInferenceVariables().contains(getType()))
			return Types.getRawTypes(IntersectionType.uncheckedFrom(resolver
					.getBounds().getInferenceVariableData().get(getType())
					.getUpperBounds()));
		return Types.getRawTypes(getType());
	}

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

	public Class<? super T> getGenericDeclaration() {
		return getRawType();
	}

	public Type getDeclaringType() {
		return getType();
	}

	public boolean isPrimitive() {
		return Types.isPrimitive(getType());
	}

	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(getType());
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> wrap() {
		if (isPrimitive())
			return (TypeToken<T>) of(Types.wrap(getRawType()));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeToken<T> unwrap() {
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

	public List<TypeVariable<?>> getAllTypeParameters() {
		return ParameterizedTypes.getAllTypeParameters(getRawType());
	}

	public Map<TypeVariable<?>, Type> getAllTypeArguments() {
		if (getType() instanceof ParameterizedType)
			return ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) getType());
		else
			return Collections.emptyMap();
	}

	public Type getTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(getRawType(), type);
	}

	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveType(TypeToken<U> type) {
		return (TypeToken<? extends U>) of(getInternalResolver().resolveType(
				type.getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (superclass.equals(getRawType()))
			return (TypeToken<? extends U>) this;

		if (!ParameterizedTypes.isGeneric(superclass))
			return of(superclass);

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(superclass,
				new HashMap<>());

		getInternalResolver().resolveTypeHierarchy(getType(), superclass);

		return (TypeToken<? extends U>) of(getInternalResolver().resolveType(
				superclass, parameterizedType));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveSubtypeParameters(Class<U> subclass) {
		if (subclass.equals(getRawType()))
			return (TypeToken<? extends U>) this;

		if (!ParameterizedTypes.isGeneric(subclass))
			return of(subclass);

		ParameterizedType parameterizedType = (ParameterizedType) ParameterizedTypes
				.uncheckedFrom(subclass, new HashMap<>());

		getInternalResolver().resolveTypeHierarchy(parameterizedType, getRawType());

		return (TypeToken<? extends U>) of(getInternalResolver().resolveType(
				subclass, parameterizedType));
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

	@SuppressWarnings("unchecked")
	private Set<? extends Invokable<? super T, ?>> getMethods(
			Predicate<Method> filter) {
		Stream<Method> methodStream = getRawTypes().stream().flatMap(
				t -> Arrays.stream(t.getMethods()));

		if (isInterface())
			methodStream = Stream.concat(methodStream,
					Arrays.stream(Object.class.getMethods()));

		System.out.println(getResolver().getBounds());

		return methodStream
				.filter(filter)
				.map(
						m -> Invokable.from(m,
								(TypeToken<? super T>) resolveSupertypeParameters(m
										.getDeclaringClass()), of(m.getGenericReturnType())))
				.collect(Collectors.toSet());
	}

	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<T, ? extends T> resolveConstructorOverload(
			Type... parameters) {
		return resolveConstructorOverload(Arrays.asList(parameters));
	}

	public Invokable<T, ? extends T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		Set<? extends Invokable<T, ? extends T>> candidates = Invokable
				.resolveApplicableCandidates(getConstructors(), parameters);

		return Invokable.resolveMostSpecificCandidate(candidates);
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		return resolveMethodOverload(name, Arrays.asList(parameters));
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		Set<? extends Invokable<? super T, ?>> candidates = getMethods(m -> m
				.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		candidates = Invokable.resolveApplicableCandidates(candidates, parameters);

		return Invokable.resolveMostSpecificCandidate(candidates);
	}
}
