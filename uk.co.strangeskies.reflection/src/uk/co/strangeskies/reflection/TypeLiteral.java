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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;
import uk.co.strangeskies.utilities.tuples.Pair;

public class TypeLiteral<T> {
	private final Type type;
	private final Class<? super T> rawType;

	private Resolver resolver;

	private static ComputingMap<Type, Property<Resolver, Resolver>> RESOLVER_CACHE = new LRUCacheComputingMap<>(
			type -> new IdentityProperty<>(), 200, true);

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
			} while (!TypeLiteral.class.equals(superClass));

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

	public static TypeLiteral<?> fromString(String typeString) {
		return from(Types.fromString(typeString));
	}

	public boolean isAbstract() {
		return Types.isAbstract(rawType);
	}

	public boolean isFinal() {
		return Types.isFinal(rawType);
	}

	public boolean isInterface() {
		return Types.isInterface(rawType);
	}

	public boolean isPrivate() {
		return Types.isPrivate(rawType);
	}

	public boolean isProtected() {
		return Types.isProtected(rawType);
	}

	public boolean isPublic() {
		return Types.isPublic(rawType);
	}

	public boolean isStatic() {
		return Types.isStatic(rawType);
	}

	public boolean isWildcard() {
		return type instanceof WildcardType;
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<? extends T> getWildcardExtending() {
		return (TypeLiteral<? extends T>) from(WildcardTypes.upperBounded(type));
	}

	@SuppressWarnings("unchecked")
	public TypeLiteral<? super T> getWildcardSuper() {
		return (TypeLiteral<? super T>) from(WildcardTypes.lowerBounded(type));
	}

	public Class<?> getNonStaticallyEnclosingClass() {
		return Types.getNonStaticallyEnclosingClass(rawType);
	}

	@SuppressWarnings("unchecked")
	public Class<? super T> getRawType() {
		return (Class<? super T>) Types.getRawType(getType());
	}

	public Set<Class<?>> getRawTypes() {
		return Types.getRawTypes(getType());
	}

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

	public String toString() {
		return Types.toString(type);
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

	public boolean isContainedBy(TypeLiteral<?> type) {
		return isContainedBy(type.getType());
	}

	public boolean isContainedBy(Type type) {
		return Types.isContainedBy(type, this.type);
	}

	public boolean isContainingOf(TypeLiteral<?> type) {
		return isContainingOf(type.getType());
	}

	public boolean isContainingOf(Type type) {
		return Types.isContainedBy(this.type, type);
	}

	public List<TypeVariable<?>> getAllTypeParameters() {
		return ParameterizedTypes.getAllTypeParameters(rawType);
	}

	public Map<TypeVariable<?>, Type> getAllTypeArguments() {
		if (type instanceof ParameterizedType)
			return ParameterizedTypes.getAllTypeArguments((ParameterizedType) type);
		else
			return Collections.emptyMap();
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
		if (superclass.equals(rawType))
			return (TypeLiteral<? extends U>) this;

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
		if (subclass.equals(rawType))
			return (TypeLiteral<? extends U>) this;

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

	@SuppressWarnings("unchecked")
	public <V> TypeLiteral<T> withTypeArgument(TypeParameter<V> parameter,
			TypeLiteral<V> argument) {
		return (TypeLiteral<T>) withTypeArgument(parameter.getType(),
				argument.getType());
	}

	@SuppressWarnings("unchecked")
	public <V> TypeLiteral<T> withTypeArgument(TypeParameter<V> parameter,
			Class<V> argument) {
		return (TypeLiteral<T>) withTypeArgument(parameter.getType(), argument);
	}

	private TypeLiteral<?> withTypeArgument(TypeVariable<?> parameter,
			Type argument) {
		return TypeLiteral.from(new TypeSubstitution().where(parameter, argument)
				.resolve(type));
	}

	@SuppressWarnings("unchecked")
	public Set<Invokable<T, T>> getConstructors() {
		return Arrays.stream(getRawType().getConstructors())
				.map(m -> Invokable.from((Constructor<T>) m, this))
				.collect(Collectors.toSet());
	}

	public Set<Invokable<? super T, ?>> getMethods() {
		return getMethods(m -> true);
	}

	@SuppressWarnings("unchecked")
	private Set<Invokable<? super T, ?>> getMethods(Predicate<Method> filter) {
		Stream<Method> methodStream = Arrays.stream(getRawType().getMethods());

		if (isInterface())
			methodStream = Stream.concat(methodStream,
					Arrays.stream(Object.class.getMethods()));

		return methodStream
				.filter(filter)
				.map(
						m -> Invokable.from(m,
								(TypeLiteral<? super T>) resolveSupertypeParameters(m
										.getDeclaringClass()), TypeLiteral.from(m
										.getGenericReturnType()))).collect(Collectors.toSet());
	}

	public Set<? extends Invokable<? super T, ?>> getInvokables() {
		Set<Invokable<? super T, ?>> invokables = new HashSet<>();
		invokables.addAll(getConstructors());
		invokables.addAll(getMethods());
		return invokables;
	}

	public Invokable<T, T> resolveConstructorOverload(Type... parameters) {
		return resolveConstructorOverload(Arrays.asList(parameters));
	}

	public Invokable<T, T> resolveConstructorOverload(
			List<? extends Type> parameters) {
		Set<? extends Invokable<T, T>> candidates = resolveApplicableCandidates(
				getConstructors(), parameters);

		return resolveMostSpecificCandidate(candidates);
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type... parameters) {
		return resolveMethodOverload(name, Arrays.asList(parameters));
	}

	public Invokable<? super T, ?> resolveMethodOverload(String name,
			List<? extends Type> parameters) {
		/*
		 * javac falls over if you use streams for this (in the obvious way at
		 * least), don't modify without checking.
		 */
		Set<? extends Invokable<? super T, ?>> candidates = getMethods(m -> m
				.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "'.");

		candidates = resolveApplicableCandidatesCapture(candidates, parameters);

		return resolveMostSpecificCandidateCapture(candidates);
	}

	/*
	 * Javac is apparently unable to deal with this capture properly unless we
	 * force it.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<? extends Invokable<? super T, ?>> resolveApplicableCandidatesCapture(
			Set<? extends Invokable<? super T, ?>> candidates,
			List<? extends Type> parameters) {
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

	public Type getComponentType() {
		return Types.getComponentType(type);
	}

	/*
	 * Javac is apparently unable to deal with this capture properly unless we
	 * force it.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Invokable<? super T, ?> resolveMostSpecificCandidateCapture(
			Set<? extends Invokable<? super T, ?>> candidates) {
		return resolveMostSpecificCandidate((Set) candidates);
	}

	private <R> Invokable<T, R> resolveMostSpecificCandidate(
			Set<? extends Invokable<T, R>> candidates) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		Set<Invokable<T, R>> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<Invokable<T, R>> overrideCandidateIterator = mostSpecificSoFar
				.iterator();
		Invokable<T, R> mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			Invokable<T, R> candidate = overrideCandidateIterator.next();

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

	private <R> Set<Invokable<T, R>> resolveMostSpecificCandidateSet(
			Set<? extends Invokable<T, R>> candidates) {
		List<Invokable<T, R>> remainingCandidates = new ArrayList<>(candidates);

		/*
		 * For each remaining candidate in the list...
		 */
		for (int first = 0; first < remainingCandidates.size(); first++) {
			Invokable<T, R> firstCandidate = remainingCandidates.get(first);

			/*
			 * Compare with each other remaining candidate...
			 */
			for (int second = first + 1; second < remainingCandidates.size(); second++) {
				Invokable<T, R> secondCandidate = remainingCandidates.get(second);

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

	public Pair<Boolean, Boolean> compareCandidates(
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

	private boolean compareGenericCandidate(Invokable<?, ?> firstCandidate,
			Invokable<?, ?> genericCandidate) {
		Resolver resolver = genericCandidate.getResolver();

		try {
			int parameters = firstCandidate.getParameters().size();
			if (firstCandidate.isVariableArity()) {
				parameters--;

				new ConstraintFormula(Kind.SUBTYPE, firstCandidate.getParameters().get(
						parameters), genericCandidate.getParameters().get(parameters))
						.reduceInto(resolver.getBounds());
			}

			for (int i = 0; i < parameters; i++) {
				new ConstraintFormula(Kind.SUBTYPE, firstCandidate.getParameters().get(
						i), genericCandidate.getParameters().get(i)).reduceInto(resolver
						.getBounds());
			}

			resolver.validate();
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
