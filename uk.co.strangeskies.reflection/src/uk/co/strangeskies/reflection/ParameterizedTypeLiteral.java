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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

public class ParameterizedTypeLiteral<T> implements TypeLiteral<T> {
	private final Type type;
	private final Class<? super T> rawType;

	private Resolver resolver;

	private static ComputingMap<Type, Property<Resolver, Resolver>> RESOLVER_CACHE = new LRUCacheComputingMap<>(
			type -> new IdentityProperty<>(), 2000, true);

	@SuppressWarnings("unchecked")
	protected ParameterizedTypeLiteral() {
		if (getClass().getSuperclass().equals(ParameterizedTypeLiteral.class))
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
			} while (!superClass.equals(ParameterizedTypeLiteral.class));

			type = resolver.resolveTypeVariable(ParameterizedTypeLiteral.class
					.getTypeParameters()[0]);
		}

		rawType = (Class<? super T>) Types.getRawType(type);
	}

	public ParameterizedTypeLiteral(Class<T> exactClass) {
		this(exactClass, exactClass);
	}

	private ParameterizedTypeLiteral(Type type, Class<? super T> rawType) {
		this.type = type;
		this.rawType = rawType;

		System.out.println("           creating TL: " + type);

		getInternalResolver();
	}

	public static TypeLiteral<?> from(Type type) {
		return from(type, Types.getRawType(type));
	}

	/*
	 * This indirection shouldn't be necessary, but javac isn't handling the
	 * wildcard capture properly without it.
	 */
	private static <T> TypeLiteral<? extends T> from(Type type, Class<T> rawType) {
		return new ParameterizedTypeLiteral<>(type, rawType);
	}

	public static <T> TypeLiteral<T> from(Class<T> type) {
		return new ParameterizedTypeLiteral<>(type);
	}

	public static TypeLiteral<?> fromString(String typeString) {
		return from(Types.fromString(typeString));
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(rawType.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(rawType.getModifiers());
	}

	@Override
	public boolean isInterface() {
		return Modifier.isInterface(rawType.getModifiers());
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate(rawType.getModifiers());
	}

	@Override
	public boolean isProtected() {
		return Modifier.isProtected(rawType.getModifiers());
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(rawType.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(rawType.getModifiers());
	}

	@Override
	public Resolver getResolver() {
		return new Resolver(getInternalResolver());
	}

	private Resolver getInternalResolver() {
		if (resolver == null) {
			Property<Resolver, Resolver> resolver;
			synchronized (RESOLVER_CACHE) {
				resolver = RESOLVER_CACHE.get(type);
				if (resolver == null) {
					resolver = RESOLVER_CACHE.putGet(type);
					resolver.set(new Resolver());
					// TODO move following out of sync block:
					resolver.get().incorporateType(type);
				}
			}
			this.resolver = resolver.get();
		}

		return resolver;
	}

	@Override
	public String toString() {
		return Types.toString(type);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Class<? super T> getGenericDeclaration() {
		return getRawType();
	}

	@Override
	public Type getDeclaringType() {
		return getType();
	}

	@Override
	public boolean isPrimitive() {
		return Types.isPrimitive(type);
	}

	@Override
	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TypeLiteral<T> wrap() {
		if (isPrimitive())
			return (TypeLiteral<T>) from(Types.wrap(rawType));
		else
			return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) from(Types.unwrap(rawType));
		else
			return this;
	}

	@Override
	public boolean isAssignableTo(TypeLiteral<?> type) {
		return isAssignableTo(type.getType());
	}

	@Override
	public boolean isAssignableTo(Type type) {
		return Types.isAssignable(this.type, type);
	}

	@Override
	public boolean isAssignableFrom(TypeLiteral<?> type) {
		return isAssignableFrom(type.getType());
	}

	@Override
	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, this.type);
	}

	@Override
	public List<TypeVariable<?>> getTypeParameters() {
		return ParameterizedTypes.getAllTypeParameters(rawType);
	}

	@Override
	public Type getTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(rawType, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return (TypeLiteral<? extends U>) ParameterizedTypeLiteral
				.from(getInternalResolver().resolveType(type.getType()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (!ParameterizedTypes.isGeneric(superclass))
			return ParameterizedTypeLiteral.from(superclass);

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(superclass,
				new HashMap<>());

		resolveTypeHierarchy(getInternalResolver(), type, superclass);

		return (TypeLiteral<? extends U>) ParameterizedTypeLiteral
				.from(getInternalResolver().resolveType(superclass, parameterizedType));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSubtypeParameters(Class<U> subclass) {
		if (!ParameterizedTypes.isGeneric(subclass))
			return ParameterizedTypeLiteral.from(subclass);

		ParameterizedType parameterizedType = (ParameterizedType) ParameterizedTypes
				.uncheckedFrom(subclass, new HashMap<>());

		resolveTypeHierarchy(getInternalResolver(), parameterizedType, rawType);

		return (TypeLiteral<? extends U>) ParameterizedTypeLiteral
				.from(getInternalResolver().resolveType(subclass, parameterizedType));
	}

	private static void resolveTypeHierarchy(Resolver resolver, Type subtype,
			Class<?> superclass) {
		Class<?> subclass = Types.getRawType(subtype);

		if (!superclass.isAssignableFrom(subclass))
			throw new IllegalArgumentException("Type '" + subtype
					+ "' is not a valid subtype of '" + superclass + "'.");

		Class<?> finalSubclass2 = subclass;
		Function<Type, Type> inferenceVariables = t -> {
			if (t instanceof TypeVariable)
				return resolver.getInferenceVariable(finalSubclass2,
						(TypeVariable<?>) t);
			else
				return null;
		};
		while (!subclass.equals(superclass)) {
			Set<Type> lesserSubtypes = new HashSet<>(Arrays.asList(subclass
					.getGenericInterfaces()));
			if (subclass.getSuperclass() != null)
				lesserSubtypes.addAll(Arrays.asList(subclass.getGenericSuperclass()));

			subtype = lesserSubtypes.stream()
					.filter(t -> superclass.isAssignableFrom(Types.getRawType(t)))
					.findAny().get();
			subtype = new TypeSubstitution(inferenceVariables).resolve(subtype);

			resolver.incorporateType(subtype);
			subclass = Types.getRawType(subtype);

			Class<?> finalSubclass = subclass;
			inferenceVariables = t -> {
				if (t instanceof TypeVariable)
					return resolver.getInferenceVariable(finalSubclass,
							(TypeVariable<?>) t);
				else
					return null;
			};
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> TypeLiteral<T> withTypeArgument(TypeParameter<V> parameter,
			TypeLiteral<V> argument) {
		return (TypeLiteral<T>) withTypeArgument(parameter.getType(),
				argument.getType());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> TypeLiteral<T> withTypeArgument(TypeParameter<V> parameter,
			Class<V> argument) {
		return (TypeLiteral<T>) withTypeArgument(parameter.getType(), argument);
	}

	private TypeLiteral<?> withTypeArgument(TypeVariable<?> parameter,
			Type argument) {
		return ParameterizedTypeLiteral.from(new TypeSubstitution().where(
				parameter, argument).resolve(type));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> Invokable.from((Constructor<T>) m, this))
				.collect(Collectors.toSet());
	}

	@Override
	public Set<Invokable<T, ?>> getMethods() {
		return getMethods(m -> true);
	}

	private Set<Invokable<T, ?>> getMethods(Predicate<Method> filter) {
		// TODO include inherited methods.
		return Arrays
				.stream(getRawType().getMethods())
				.filter(filter)
				.map(
						m -> Invokable.from(m, this,
								ParameterizedTypeLiteral.from(m.getGenericReturnType())))
				.collect(Collectors.toSet());
	}

	@Override
	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	@Override
	public Invokable<T, T> resolveConstructorOverload(Type... parameters) {
		return resolveConstructorOverload(Arrays.asList(parameters));
	}

	@Override
	public Invokable<T, T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		Set<? extends Invokable<T, T>> candidates = resolveApplicableCandidates(
				getConstructors(), parameters);

		return resolveMostSpecificCandidate(candidates, parameters);
	}

	@Override
	public Invokable<T, ?> resolveMethodOverload(String name, Type... parameters) {
		return resolveMethodOverload(name, Arrays.asList(parameters));
	}

	@Override
	public Invokable<T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		/*
		 * javac falls over if you use streams for this (in the obvious way at
		 * least), don't modify without checking.
		 */
		Set<? extends Invokable<T, ?>> candidates = getMethods(m -> m.getName()
				.equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		candidates = resolveApplicableCandidatesCapture(candidates, parameters);

		return resolveMostSpecificCandidateCapture(candidates, parameters);
	}

	/*
	 * Javac is apparently unable to deal with this capture properly unless we
	 * force it.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<? extends Invokable<T, ?>> resolveApplicableCandidatesCapture(
			Set<? extends Invokable<T, ?>> candidates, List<? extends Type> parameters) {
		return resolveApplicableCandidates((Set) candidates, parameters);
	}

	private <R> Set<? extends Invokable<T, R>> resolveApplicableCandidates(
			Set<? extends Invokable<T, R>> candidates, List<? extends Type> parameters) {
		List<RuntimeException> failures = new ArrayList<>();

		Set<? extends Invokable<T, R>> compatibleCandidates = filterOverloadCandidates(
				candidates, i -> i.withLooseApplicability(parameters), failures::add);

		if (compatibleCandidates.isEmpty())
			compatibleCandidates = filterOverloadCandidates(candidates,
					i -> i.withVariableArityApplicability(parameters), failures::add);
		else {
			Set<? extends Invokable<T, R>> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withStrictApplicability(parameters), failures::add);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw new TypeInferenceException("Parameters '" + parameters
					+ "' are not applicable to invokable candidates '" + candidates
					+ "'.", failures.iterator().next());

		return compatibleCandidates;
	}

	private <R> Set<? extends Invokable<T, R>> filterOverloadCandidates(
			Set<? extends Invokable<T, R>> candidates,
			Function<? super Invokable<T, R>, Invokable<T, R>> applicabilityFunction,
			Consumer<RuntimeException> failures) {
		return candidates.stream().map(i -> {
			try {
				return applicabilityFunction.apply(i);
			} catch (RuntimeException e) {
				failures.accept(e);
				return null;
			}
		}).filter(o -> o != null).collect(Collectors.toSet());
	}

	@Override
	public Type getComponentType() {
		return Types.getComponentType(type);
	}

	/*
	 * Javac is apparently unable to deal with this capture properly unless we
	 * force it.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Invokable<T, ?> resolveMostSpecificCandidateCapture(
			Set<? extends Invokable<T, ?>> candidates,
			List<? extends Type> parameterTypes) {
		return resolveMostSpecificCandidate((Set) candidates, parameterTypes);
	}

	private <R> Invokable<T, R> resolveMostSpecificCandidate(
			Set<? extends Invokable<T, R>> candidates,
			List<? extends Type> parameterTypes) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		/*
		 * TODO consider generics in invokable overload specificity
		 * 
		 * Find which candidates have the joint most specific parameters, one
		 * parameter at a time.
		 */
		Set<Invokable<T, R>> mostSpecificSoFar = new HashSet<>(candidates);
		int parameters = candidates.iterator().next().getParameters().size();
		for (int i = 0; i < parameters; i++) {
			Set<Invokable<T, ?>> mostSpecificForParameter = new HashSet<>();

			TypeLiteral<?> mostSpecificParameterType = ParameterizedTypeLiteral
					.from(candidates.iterator().next().getParameters().get(i));

			for (Invokable<T, ?> overloadCandidate : candidates) {
				TypeLiteral<?> parameterClass = ParameterizedTypeLiteral
						.from(overloadCandidate.getParameters().get(i));

				if (parameterClass.isAssignableFrom(mostSpecificParameterType)) {
					mostSpecificParameterType = parameterClass;

					if (!mostSpecificParameterType.isAssignableFrom(parameterClass))
						mostSpecificForParameter.clear();
					mostSpecificForParameter.add(overloadCandidate);
				} else if (!mostSpecificParameterType.isAssignableFrom(parameterClass)) {
					throw new TypeInferenceException("Cannot resolve method ambiguity.");
				}
			}

			mostSpecificSoFar.retainAll(mostSpecificForParameter);

			if (mostSpecificSoFar.isEmpty())
				throw new TypeInferenceException("Cannot resolve method ambiguity.");
		}

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<Invokable<T, R>> overrideCandidateIterator = mostSpecificSoFar
				.iterator();
		Invokable<T, R> mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			Invokable<T, R> candidate = overrideCandidateIterator.next();
			mostSpecific = candidate.getExecutable().getDeclaringClass()
					.isAssignableFrom(mostSpecific.getExecutable().getDeclaringClass()) ? candidate
					: mostSpecific;
		}

		return mostSpecific;
	}
}
