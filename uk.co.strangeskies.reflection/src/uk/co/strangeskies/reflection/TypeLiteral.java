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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeLiteral<T> implements GenericTypeContainer<Class<? super T>> {
	private final Type type;
	private final Class<? super T> rawType;

	private Resolver resolver;

	@SuppressWarnings("unchecked")
	protected TypeLiteral() {
		if (getClass().getSuperclass().equals(TypeLiteral.class))
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
			} while (!superClass.equals(TypeLiteral.class));

			type = resolver
					.resolveTypeVariable(TypeLiteral.class.getTypeParameters()[0]);
		}

		rawType = (Class<? super T>) Types.getRawType(type);
	}

	public TypeLiteral(Class<T> exactClass) {
		this(exactClass, exactClass);
	}

	private TypeLiteral(Type type, Class<? super T> rawType) {
		this.type = type;
		this.rawType = rawType;

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
		return new TypeLiteral<>(type, rawType);
	}

	public static <T> TypeLiteral<T> from(Class<T> type) {
		return new TypeLiteral<>(type);
	}

	public static <T> TypeLiteral<T> fromString(String typeString) {
		throw new UnsupportedOperationException(); // TODO
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(rawType.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(rawType.getModifiers());
	}

	public boolean isInterface() {
		return Modifier.isInterface(rawType.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(rawType.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(rawType.getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(rawType.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(rawType.getModifiers());
	}

	public Resolver getResolver() {
		return new Resolver(getInternalResolver());
	}

	private Resolver getInternalResolver() {
		if (resolver == null) {
			resolver = new Resolver();
			resolver.incorporateType(type);
		}
		return resolver;
	}

	@Override
	public String toString() {
		return Types.toString(type);
	}

	public Type getType() {
		return type;
	}

	public Class<? super T> getRawType() {
		return rawType;
	}

	@Override
	public Class<? super T> getGenericDeclaration() {
		return getRawType();
	}

	@Override
	public Type getDeclaringType() {
		return getType();
	}

	public boolean isPrimitive() {
		return Types.isPrimitive(type);
	}

	public boolean isPrimitiveWrapper() {
		return Types.isPrimitiveWrapper(type);
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> wrap() {
		if (isPrimitive())
			return (TypeLiteral<T>) from(Types.wrap(rawType));
		else
			return this;
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<T> unwrap() {
		if (isPrimitiveWrapper())
			return (TypeLiteral<T>) from(Types.unwrap(rawType));
		else
			return this;
	}

	public boolean isAssignableTo(TypeLiteral<?> type) {
		return isAssignableTo(type.getType());
	}

	public boolean isAssignableTo(Type type) {
		return Types.isAssignable(this.type, type);
	}

	public boolean isAssignableFrom(TypeLiteral<?> type) {
		return isAssignableFrom(type.getType());
	}

	public boolean isAssignableFrom(Type type) {
		return Types.isAssignable(type, this.type);
	}

	public List<TypeVariable<?>> getTypeParameters() {
		return ParameterizedTypes.getAllTypeParameters(rawType);
	}

	public Type getTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(rawType, type);
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveType(TypeLiteral<U> type) {
		return (TypeLiteral<? extends U>) TypeLiteral.from(getInternalResolver()
				.resolveType(type.getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (!ParameterizedTypes.isGeneric(superclass))
			return TypeLiteral.from(superclass);

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(superclass,
				new HashMap<>());

		resolveTypeHierarchy(getInternalResolver(), type, superclass);

		return (TypeLiteral<? extends U>) TypeLiteral.from(getInternalResolver()
				.resolveType(superclass, parameterizedType));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeLiteral<? extends U> resolveSubtypeParameters(Class<U> subclass) {
		if (!ParameterizedTypes.isGeneric(subclass))
			return TypeLiteral.from(subclass);

		ParameterizedType parameterizedType = (ParameterizedType) ParameterizedTypes
				.uncheckedFrom(subclass, new HashMap<>());

		resolveTypeHierarchy(getInternalResolver(), parameterizedType, rawType);

		return (TypeLiteral<? extends U>) TypeLiteral.from(getInternalResolver()
				.resolveType(subclass, parameterizedType));
	}

	private static void resolveTypeHierarchy(Resolver resolver, Type subtype,
			Class<?> superclass) {
		Class<?> subclass = Types.getRawType(subtype);

		if (!superclass.isAssignableFrom(subclass))
			throw new IllegalArgumentException();

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

	@SuppressWarnings("unchecked")
	public TypeLiteral<? extends T> withTypeArguments(
			Map<TypeVariable<?>, Type> arguments) {
		Resolver resolver = new Resolver(getInternalResolver());

		for (TypeVariable<?> parameter : arguments.keySet())
			resolver.incorporateInstantiation(parameter, arguments.get(parameter));

		return (TypeLiteral<? extends T>) TypeLiteral.from(resolver
				.resolveType(rawType));
	}

	public TypeLiteral<? extends T> withTypeArgument(TypeVariable<?> parameter,
			Type argument) {
		HashMap<TypeVariable<?>, Type> arguments = new HashMap<>();
		arguments.put(parameter, argument);
		return withTypeArguments(arguments);
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> parameter, TypeLiteral<V> argument) {
		return withTypeArgument(parameter.getType(), argument.getType());
	}

	public <V> TypeLiteral<? extends T> withTypeArgument(
			TypeParameter<V> parameter, Class<V> argument) {
		return withTypeArgument(parameter.getType(), argument);
	}

	@SuppressWarnings("unchecked")
	public Set<Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> Invokable.from((Constructor<T>) m, this))
				.collect(Collectors.toSet());
	}

	public Set<Invokable<T, ?>> getMethods() {
		// TODO include inherited methods.
		return Arrays
				.stream(getRawType().getMethods())
				.map(
						m -> Invokable.from(m, this,
								TypeLiteral.from(m.getGenericReturnType())))
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
		return resolveInvokableOverload(getConstructors(), parameters);
	}

	public Invokable<T, ?> resolveMethodOverload(String name, Type... parameters) {
		return resolveMethodOverload(name, Arrays.asList(parameters));
	}

	public Invokable<T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		/*
		 * javac falls over if you use streams for this (in the obvious way at
		 * least), don't modify without checking.
		 */
		Set<Invokable<T, ?>> candidates = getMethods();
		Iterator<Invokable<T, ?>> candidateIterator = candidates.iterator();
		while (candidateIterator.hasNext())
			if (!candidateIterator.next().getGenericDeclaration().getName()
					.equals(name))
				candidateIterator.remove();

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		return resolveInvokableOverload(candidates, parameters);
	}

	private <U, R> Invokable<U, ? extends R> resolveInvokableOverload(
			Set<? extends Invokable<U, ? extends R>> candidates,
			List<? extends Type> parameters) {
		Set<RuntimeException> failures = new HashSet<>();

		Set<? extends Invokable<U, ? extends R>> compatibleCandidates = filterOverloadCandidates(
				candidates, i -> i.withLooseApplicability(parameters), failures::add);

		if (compatibleCandidates.isEmpty())
			compatibleCandidates = filterOverloadCandidates(candidates,
					i -> i.withVariableArityApplicability(parameters), failures::add);
		else {
			Set<? extends Invokable<U, ? extends R>> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withStrictApplicability(parameters), failures::add);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw failures.iterator().next();

		return compatibleCandidates.iterator().next();
	}

	private static <U, R> Set<? extends Invokable<U, ? extends R>> filterOverloadCandidates(
			Set<? extends Invokable<U, ? extends R>> candidates,
			Function<? super Invokable<U, ? extends R>, Invokable<U, ? extends R>> applicabilityFunction,
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

	public Type getComponentType() {
		return Types.getComponentType(type);
	}
}
