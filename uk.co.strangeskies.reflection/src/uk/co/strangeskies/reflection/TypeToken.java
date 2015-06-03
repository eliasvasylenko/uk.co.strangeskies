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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.DeepCopyable;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;
import uk.co.strangeskies.utilities.collection.computingmap.LRUCacheComputingMap;

/**
 * <p>
 * TypeToken provides reflective operations and services over the Java type
 * system. It is analogous to {@code Class<?>}, but provides access to a much
 * richer set of tools, and can be used over the domain of all types, not just
 * raw types.
 * 
 * <p>
 * TypeToken is effectively immutable, though may perform shared caching of
 * results transparently to the user. Like Class, A TypeToken will always be
 * parameterised with the type it reflects over when used as intended.
 * 
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          This is the type which the TypeToken object references.
 */
public class TypeToken<T> implements DeepCopyable<TypeToken<T>> {
	/**
	 * Treatment of wildcards for {@link TypeToken}s created over parameterized
	 * types.
	 * 
	 * @author Elias N Vasylenko
	 */
	public enum Wildcards {
		/**
		 * Wildcards will be left alone, though capture may be necessary for
		 * incorporation into backing {@link Resolver}, as wildcards alone do not
		 * always fully specify valid bounds.
		 */
		PRESERVE,
		/**
		 * Wildcards should be substituted with inference variables, with
		 * appropriate bounds incorporated based on both type variable bounds and
		 * wildcard bounds.
		 */
		INFER,
		/**
		 * Wildcards should be substituted with fresh {@link TypeVariableCapture}
		 * instances, as per
		 * {@link TypeVariableCapture#captureWildcardArguments(ParameterizedType)} .
		 */
		CAPTURE;
	}

	/**
	 * Specifies behaviour of wildcards. If the annotated type is a wildcard type,
	 * it will behave according to {@link Wildcards#PRESERVE}, and if it is a
	 * parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Preserve {}

	/**
	 * Specifies behaviour of wildcards. If the annotated type is a wildcard type,
	 * it will behave according to {@link Wildcards#INFER}, and if it is a
	 * parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Infer {}

	/**
	 * Specifies behaviour of wildcards. If the annotated type is a wildcard type,
	 * it will behave according to {@link Wildcards#CAPTURE}, and if it is a
	 * parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Capture {}

	/*
	 * TODO we could do a better job of caching here if we cached parameterized
	 * types *before* type variable capture, then substituted any captures out of
	 * the cached resolver for new ones.
	 */
	private static ComputingMap<Type, Resolver> RESOLVER_CACHE = new LRUCacheComputingMap<>(
			(Type type) -> {
				Resolver resolver = new Resolver();
				resolver.incorporateType(type);
				return resolver;
			}, 200, true);

	private Resolver resolver;

	private final Type type;
	private final Class<? super T> rawType;

	@SuppressWarnings("unchecked")
	protected TypeToken() {
		type = resolveAnnotatedSuperclassParameter();

		rawType = (Class<? super T>) Types.getRawType(type);
	}

	@SuppressWarnings("unchecked")
	protected TypeToken(AnnotatedType type) {
		this.type = dealWithAnnotatedWildcards(type, new HashMap<>());

		rawType = (Class<? super T>) Types.getRawType(this.type);
	}

	private Type resolveAnnotatedSuperclassParameter() {
		Class<?> subclass = getClass();

		Type type;

		Map<TypeVariable<?>, Type> resolvedParameters = new HashMap<>();

		do {
			AnnotatedType annotatedType = subclass.getAnnotatedSuperclass();

			if (annotatedType instanceof AnnotatedParameterizedType) {
				type = dealWithAnnotatedWildcards(annotatedType, resolvedParameters);

				resolvedParameters = ParameterizedTypes
						.getAllTypeArguments((ParameterizedType) type);
			} else {
				type = annotatedType.getType();

				resolvedParameters.clear();
			}

			subclass = subclass.getSuperclass();
		} while (!subclass.equals(TypeToken.class));

		return ((ParameterizedType) type).getActualTypeArguments()[0];
	}

	private Type dealWithAnnotatedWildcards(AnnotatedType annotatedType,
			Map<TypeVariable<?>, Type> resolvedParameters) {
		Wildcards behaviour = annotatedType.isAnnotationPresent(Preserve.class) ? Wildcards.PRESERVE
				: annotatedType.isAnnotationPresent(Infer.class) ? Wildcards.INFER
						: annotatedType.isAnnotationPresent(Capture.class) ? Wildcards.CAPTURE
								: null;

		if (annotatedType instanceof AnnotatedParameterizedType) {
			/*
			 * Deal with annotations on types mentioned by parameters, preserving any
			 * parameters which are wildcards themselves.
			 */
			Type[] arguments = dealWithAnnotatedWildcards(
					((AnnotatedParameterizedType) annotatedType)
							.getAnnotatedActualTypeArguments(),
					resolvedParameters);

			/*
			 * Collect all arguments into a mapping from type variables, including on
			 * enclosing types.
			 */
			Map<TypeVariable<?>, Type> allArguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) annotatedType.getType());
			TypeVariable<?>[] parameters = Types.getRawType(annotatedType.getType())
					.getTypeParameters();
			for (int i = 0; i < arguments.length; i++)
				allArguments.put(parameters[i], arguments[i]);

			/*
			 * New parameterized type
			 * 
			 * TODO override "behaviour" with annotations on each parameter.
			 */
			ParameterizedType parameterizedType = (ParameterizedType) ParameterizedTypes
					.uncheckedFrom(Types.getRawType(annotatedType.getType()),
							allArguments);
			if (allArguments.values().stream()
					.anyMatch(WildcardType.class::isInstance)) {
				if (behaviour == Wildcards.CAPTURE) {
					return TypeVariableCapture
							.captureWildcardArguments(parameterizedType);
				} else if (behaviour == Wildcards.INFER) {
					if (resolver == null)
						resolver = new Resolver();
					return resolver.inferOverTypeArguments(parameterizedType);
				}
			}
			return parameterizedType;
		} else if (annotatedType instanceof AnnotatedTypeVariable) {
			return resolvedParameters.getOrDefault(annotatedType.getType(),
					annotatedType.getType());
		} else if (annotatedType instanceof AnnotatedWildcardType) {
			AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) annotatedType;
			WildcardType wildcardType;

			if (annotatedWildcardType.getAnnotatedLowerBounds().length > 0) {
				wildcardType = WildcardTypes
						.lowerBounded(dealWithAnnotatedWildcards(
								annotatedWildcardType.getAnnotatedLowerBounds(),
								resolvedParameters));

			} else if (annotatedWildcardType.getAnnotatedUpperBounds().length > 0) {
				wildcardType = WildcardTypes
						.upperBounded(dealWithAnnotatedWildcards(
								annotatedWildcardType.getAnnotatedUpperBounds(),
								resolvedParameters));

			} else {
				wildcardType = WildcardTypes.unbounded();
			}

			return wildcardType;
		} else if (annotatedType instanceof AnnotatedArrayType) {
			return annotatedType.getType();
		} else {
			return annotatedType.getType();
		}
	}

	private Type[] dealWithAnnotatedWildcards(AnnotatedType[] annotatedTypes,
			Map<TypeVariable<?>, Type> resolvedParameters) {
		Type[] types = new Type[annotatedTypes.length];
		for (int i = 0; i < types.length; i++)
			types[i] = dealWithAnnotatedWildcards(annotatedTypes[i],
					resolvedParameters);
		return types;
	}

	protected TypeToken(Type type) {
		this(null, type);
	}

	private TypeToken(Type type, Wildcards wildcards) {
		this(null, type, wildcards);
	}

	private TypeToken(Resolver resolver, Type type) {
		this(resolver, type, Wildcards.PRESERVE);
	}

	@SuppressWarnings("unchecked")
	private TypeToken(Resolver resolver, Type type, Wildcards wildcards) {
		this.resolver = resolver;

		if (type instanceof Class && resolver != null)
			resolver.incorporateTypeParameters((Class<?>) type);
		else
			type = dealWithWildcards(type, wildcards);
		this.type = type;

		this.rawType = (Class<? super T>) (resolver == null ? Types
				.getRawType(this.type) : resolver.getRawTypes(this.type).iterator()
				.next());
	}

	private Type dealWithWildcards(Type type, Wildcards wildcards) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			if (wildcards == Wildcards.CAPTURE) {
				type = TypeVariableCapture.captureWildcardArguments(parameterizedType);
			} else if (wildcards == Wildcards.INFER) {
				if (resolver == null)
					resolver = new Resolver();
				type = resolver.inferOverTypeArguments(parameterizedType);
			}
		} else if (type instanceof GenericArrayType) {
			GenericArrayType arrayType = (GenericArrayType) type;

			if (wildcards == Wildcards.CAPTURE) {
				type = TypeVariableCapture.captureWildcardArguments(arrayType);
			} else if (wildcards == Wildcards.INFER) {
				if (resolver == null)
					resolver = new Resolver();
				type = resolver.inferOverTypeArguments(arrayType);
			}
		} else if (type instanceof WildcardType) {
			if (wildcards == Wildcards.CAPTURE) {
				type = TypeVariableCapture.captureWildcard((WildcardType) type);
			} else if (wildcards == Wildcards.INFER) {
				if (resolver == null)
					resolver = new Resolver();
				type = resolver.inferOverWildcardType((WildcardType) type);
			}
		}

		return type;
	}

	/**
	 * Create a TypeToken for a raw class.
	 * 
	 * @param <T>
	 *          The type of the new {@link TypeToken}.
	 * @param type
	 *          The class to create a TypeToken for.
	 * @return A TypeToken over the requested class.
	 */
	public static <T> TypeToken<T> over(Class<T> type) {
		return new TypeToken<T>(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type, preserving wildcards where
	 * possible.
	 * 
	 * @param type
	 *          The requested type.
	 * @return A TypeToken over the requested type.
	 */
	public static TypeToken<?> over(Type type) {
		return new TypeToken<>(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type.
	 * 
	 * @param type
	 *          The requested type.
	 * @param wildcards
	 *          How to deal with wildcard parameters on the given type.
	 * @return A TypeToken over the requested type.
	 */
	public static TypeToken<?> over(Type type, Wildcards wildcards) {
		return new TypeToken<>(type, wildcards);
	}

	/**
	 * Create a TypeToken view over a raw type with respect to its
	 * parameterisations within a given resolver.
	 * 
	 * @param resolver
	 *          The resolution context for the type.
	 * @param rawType
	 *          The requested type.
	 * @param <T>
	 *          The requested type.
	 * @return A TypeToken over the requested type.
	 */
	public static <T> TypeToken<? extends T> over(Resolver resolver,
			Class<T> rawType) {
		resolver.incorporateTypeParameters(rawType);
		return new TypeToken<>(resolver.copy(),
				resolver.resolveTypeParameters(rawType));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TypeToken
				&& type.equals(((TypeToken<?>) obj).getType());
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public TypeToken<T> copy() {
		return this;
	}

	@Override
	public TypeToken<T> deepCopy() {
		Map<InferenceVariable, InferenceVariable> inferenceVariableSubstitutions = new HashMap<>();

		Resolver resolver = getInternalResolver()
				.withNewInferenceVariableSubstitution(inferenceVariableSubstitutions);

		return new TypeToken<T>(resolver, new TypeSubstitution(
				inferenceVariableSubstitutions::get).resolve(type));
	}

	/**
	 * Derive a new {@link TypeToken} instance, with the given bounds incorporated
	 * into the bounds of the underlying resolver. The original {@link TypeToken}
	 * will remain unmodified.
	 * 
	 * <p>
	 * All bounds are incorporated if and only if they have the potential to
	 * affect the resolution of inference variables mentioned by this type.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @return The newly derived {@link TypeToken}.
	 */
	public TypeToken<T> withBounds(BoundSet bounds) {
		return new TypeToken<T>(getResolverWithBounds(bounds), getType());
	}

	/**
	 * Derive a new {@link TypeToken} instance, with the bounds on the given
	 * inference variables, with respect to the given bound set, incorporated into
	 * the bounds of the underlying resolver. The original {@link TypeToken} will
	 * remain unmodified.
	 * 
	 * <p>
	 * All bounds are incorporated if and only if they have the potential to
	 * affect the resolution of inference variables mentioned by this type.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @param inferenceVariables
	 *          The inference variables whose bounds are to be incorporated.
	 * @return The newly derived {@link TypeToken}.
	 */
	public TypeToken<T> withBounds(BoundSet bounds,
			Collection<? extends InferenceVariable> inferenceVariables) {
		return new TypeToken<T>(getResolverWithBounds(bounds, inferenceVariables),
				getType());
	}

	/**
	 * Derive a new {@link TypeToken} instance, with the bounds on the given type
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link TypeToken} will remain unmodified.
	 * 
	 * <p>
	 * All bounds are incorporated if and only if they have the potential to
	 * affect the resolution of inference variables mentioned by this type.
	 * 
	 * @param type
	 *          The type whose bounds are to be incorporated.
	 * @return The newly derived {@link TypeToken}.
	 */
	public TypeToken<T> withBoundsFrom(TypeToken<?> type) {
		return new TypeToken<T>(getResolverWithBoundsFrom(type), getType());
	}

	/**
	 * Equivalent to the application of {@link TypeToken#over(Type)} to the result
	 * of {@link Types#fromString(String)}.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @return A TypeToken representing the type described by the String.
	 */
	public static TypeToken<?> fromString(String typeString) {
		return over(Types.fromString(typeString));
	}

	protected void validate() {
		getInternalResolver();
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as an upper bound.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will give
	 * a {@code TypeToken<? extends List<?>>}.
	 * 
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	public TypeToken<? extends T> getWildcardExtending() {
		return getExtending(Wildcards.PRESERVE);
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as an upper bound.
	 * 
	 * The wildcard will be represented according to the {@link Wildcards}
	 * argument given. Either the wildcard will be preserved, a fresh
	 * {@link TypeVariableCapture} of the wildcard type will be captured, or an
	 * {@link InferenceVariable} will be substituted.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will give
	 * a {@code TypeToken<? extends List<?>>}.
	 * 
	 * @param wildcards
	 *          How to deal with the wildcard parameter on the new type.
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? extends T> getExtending(Wildcards wildcards) {
		if (wildcards == Wildcards.INFER) {
			Resolver resolver = getResolver();
			return (TypeToken<? extends T>) new TypeToken<>(resolver,
					resolver.inferOverWildcardType(WildcardTypes.upperBounded(getType())));
		} else {
			return (TypeToken<? extends T>) over(
					WildcardTypes.upperBounded(getType()), wildcards);
		}
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as a lower bound.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will give
	 * a {@code TypeToken<? super List<?>>}.
	 * 
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	public TypeToken<? super T> getWildcardSuper() {
		return getSuper(Wildcards.PRESERVE);
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as a lower bound.
	 * 
	 * The wildcard will be represented according to the {@link Wildcards}
	 * argument given. Either the wildcard will be preserved, a fresh
	 * {@link TypeVariableCapture} of the wildcard type will be captured, or an
	 * {@link InferenceVariable} will be substituted.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will give
	 * a {@code TypeToken<? super List<?>>}.
	 * 
	 * @param wildcards
	 *          How to deal with the wildcard parameter on the new type.
	 * @return The TypeToken representing a capture of the wildcard described.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? super T> getSuper(Wildcards wildcards) {
		if (wildcards == Wildcards.INFER) {
			Resolver resolver = getResolver();
			return new TypeToken<>(resolver,
					resolver.inferOverWildcardType(WildcardTypes.lowerBounded(getType())));
		} else {
			return (TypeToken<? super T>) over(WildcardTypes.lowerBounded(getType()),
					wildcards);
		}
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
		return getInternalResolver().getRawTypes(getType());
	}

	/**
	 * This method returns a copy of the Resolver backing by this TypeToken.
	 * 
	 * @return A new Resolver object containing whichever bounds have been
	 *         internally derived from the type of this TypeToken.
	 */
	public Resolver getResolver() {
		return getInternalResolver().copy();
	}

	private Resolver getInternalResolver() {
		if (resolver == null) {
			synchronized (RESOLVER_CACHE) {
				resolver = RESOLVER_CACHE
						.putGet(getType() instanceof ParameterizedType ? TypeVariableCapture
								.captureWildcardArguments((ParameterizedType) getType())
								: getType());
			}
			return resolver;
		} else
			return resolver;
	}

	private Resolver getResolverWithBounds(BoundSet bounds) {
		Resolver resolver = getResolver();
		resolver.getBounds()
				.incorporate(bounds, getInferenceVariableDependencies());
		return resolver;
	}

	private Resolver getResolverWithBounds(BoundSet bounds,
			Collection<? extends InferenceVariable> inferenceVariables) {
		Resolver resolver = getResolver();
		Set<InferenceVariable> withMentioned = new HashSet<>(inferenceVariables);
		withMentioned.addAll(getInferenceVariableDependencies());
		resolver.getBounds().incorporate(bounds, withMentioned);
		return resolver;
	}

	private Resolver getResolverWithBoundsFrom(TypeToken<?> type) {
		return getResolverWithBounds(type.getResolver().getBounds(),
				type.getInferenceVariableDependencies());
	}

	@Override
	public String toString() {
		return Types.toString(getType());
	}

	/**
	 * The type represented by this {@link TypeToken}.
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
			return (TypeToken<T>) over(Types.wrapPrimitive(getRawType()));
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
			return (TypeToken<T>) over(Types.unwrapPrimitive(getRawType()));
		else
			return this;
	}

	/**
	 * Determine whether the type described by this {@link TypeToken} can be
	 * assigned to the type described by the given {@link TypeToken}. If either of
	 * the types mention unresolved inference variables, any bounds implied
	 * between the two bound sets will be considered.
	 * 
	 * @param type
	 *          The type to which we wish to determine assignability.
	 * @return True if the assignment is possible, false otherwise.
	 */
	public boolean isAssignableTo(TypeToken<?> type) {
		try {
			Resolver resolver = getResolverWithBoundsFrom(type);
			ConstraintFormula.reduce(Kind.LOOSE_COMPATIBILILTY, getType(),
					type.getType(), resolver.getBounds());
			return true;
		} catch (Exception e) {
			return false;
		}
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
		return isAssignableTo(over(type));
	}

	/**
	 * See {@link TypeToken#isAssignableFrom(TypeToken)}.
	 * 
	 * @param type
	 *          Forwards to {@code type}.
	 * @return As referenced method.
	 */
	public boolean isAssignableFrom(TypeToken<?> type) {
		return type.isAssignableTo(this);
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
		return isAssignableFrom(over(type));
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
		try {
			Resolver resolver = getResolverWithBoundsFrom(type);
			ConstraintFormula.reduce(Kind.CONTAINMENT, getType(), type.getType(),
					resolver.getBounds());
			return true;
		} catch (Exception e) {
			return false;
		}
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
		return isContainedBy(over(type));
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
		return type.isContainedBy(this);
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
		return isContainingOf(over(type));
	}

	/**
	 * 
	 * Derive a new type from this one, with a lower bounding on this type by a
	 * given type. The invocation will fail if the lower bound cannot be
	 * satisfied. For types which mention inference variables, this lower bounding
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *          The lower bound.
	 * @return A new type token which satisfies the bounding.
	 */
	public TypeToken<T> withLowerBound(Type type) {
		Resolver resolver = getResolver();
		resolver.addLowerBound(getType(), type);

		return new TypeToken<>(resolver, getType());
	}

	/**
	 * 
	 * Derive a new type from this one, with a lower bounding on this type by a
	 * given type. The invocation will fail if the lower bound cannot be
	 * satisfied. For types which mention inference variables, this lower bounding
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *          The lower bound.
	 * @return A new type token which satisfies the bounding.
	 */
	public TypeToken<T> withLowerBound(TypeToken<?> type) {
		Resolver resolver = getResolverWithBoundsFrom(type);
		resolver.addLowerBound(getType(), type.getType());

		return new TypeToken<>(resolver, resolveType());
	}

	/**
	 * Derive a new type from this one, with an upper bounding on this type by a
	 * given type. The invocation will fail if the lower bound cannot be
	 * satisfied. For types which mention inference variables, this lower bounding
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *          The upper bound.
	 * @return A new type token which satisfies the bounding.
	 */
	public TypeToken<T> withUpperBound(Type type) {
		Resolver resolver = getResolver();
		resolver.addUpperBound(getType(), type);

		return new TypeToken<>(resolver, getType());
	}

	/**
	 * Derive a new type from this one, with an upper bounding on this type by a
	 * given type. The invocation will fail if the lower bound cannot be
	 * satisfied. For types which mention inference variables, this lower bounding
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *          The upper bound.
	 * @return A new type token which satisfies the bounding.
	 */
	public TypeToken<T> withUpperBound(TypeToken<?> type) {
		Resolver resolver = getResolverWithBoundsFrom(type);
		resolver.addUpperBound(getType(), type.getType());

		return new TypeToken<>(resolver, resolveType());
	}

	/**
	 * For a given generic superclass of this type, determine the type arguments
	 * of the exact supertype.
	 * 
	 * @param <U>
	 *          The class of the supertype parameterization we wish to determine.
	 * @param superclass
	 *          The class of the supertype parameterization we wish to determine.
	 * @return A TypeToken over the supertype of the requested class.
	 */
	public <U> TypeToken<? extends U> resolveSupertypeParameters(
			Class<U> superclass) {
		if (!ParameterizedTypes.isGeneric(superclass))
			return TypeToken.over(superclass);

		Resolver resolver = getInternalResolver();

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(superclass,
				new HashMap<>());

		if (resolver.getBounds().getInferenceVariables().contains(getType())) {
			resolver.incorporateTypeParameters(superclass);
			parameterizedType = resolver.resolveType(parameterizedType);

			ConstraintFormula.reduce(Kind.SUBTYPE, getType(), parameterizedType,
					resolver.getBounds());
		} else
			resolver.incorporateTypeHierarchy(getRawType(), superclass);

		return over(resolver, superclass);
	}

	/**
	 * Attempt to determine the type arguments with which a subtype of a given
	 * class would be parameterized such that it be a valid subtype. This may not
	 * always be possible, but for certain subtype relations it will, based on the
	 * reduction and incorporation rules of the Java type inference algorithm.
	 * 
	 * @param <U>
	 *          The class of the subtype parameterization we wish to determine.
	 * @param subclass
	 *          The class of the subtype parameterization we wish to determine.
	 * @return A TypeToken over the best effort parameterization of the requested
	 *         class such that it be a subtype.
	 */
	public <U> TypeToken<? extends U> resolveSubtypeParameters(Class<U> subclass) {
		if (!ParameterizedTypes.isGeneric(subclass))
			return TypeToken.over(subclass);

		Resolver resolver = getInternalResolver();

		Type parameterizedType = ParameterizedTypes.uncheckedFrom(subclass,
				new HashMap<>());

		if (resolver.getBounds().getInferenceVariables().contains(getType())) {
			resolver.incorporateTypeParameters(subclass);
			parameterizedType = resolver.resolveType(parameterizedType);

			ConstraintFormula.reduce(Kind.SUBTYPE, parameterizedType, getType(),
					resolver.getBounds());
		} else
			resolver.incorporateTypeHierarchy(subclass, getRawType());

		return over(resolver, subclass);
	}

	/**
	 * Resolve a given type with respect to the type represented by this
	 * TypeToken. This means that any occurrences of TypeVariable or
	 * InferenceVariable objects will be substituted by any instantiations thereof
	 * which may be present as part of this TypeToken's type hierarchy.
	 * 
	 * <p>
	 * Inference variables which are mentioned by the given type will also be
	 * considered, along with their bounds with respect to both {@link TypeToken}
	 * instances.
	 * 
	 * @param <U>
	 *          The type we wish to resolve.
	 * @param type
	 *          The type we wish to resolve.
	 * @return A new TypeToken with all type variables and inference variables
	 *         which have instantiations resolved.
	 */
	public <U> TypeToken<U> resolveType(TypeToken<U> type) {
		return type.withBoundsFrom(this).resolve();
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
	public Type resolveType(Type type) {
		if (getType() instanceof ParameterizedType)
			type = getInternalResolver().resolveType(getRawType(), type);

		return getInternalResolver().resolveType(type);
	}

	/**
	 * <p>
	 * As {@link ParameterizedTypes#getAllTypeParameters(Class)} called on each of
	 * {@link #getRawTypes()} merged into a single list.
	 * 
	 * 
	 * <p>
	 * For type tokens over a single parameterized type, this method will return
	 * the equivalent of an invocation of
	 * {@link ParameterizedTypes#getAllTypeParameters(Class)} on that type.
	 * 
	 * 
	 * @return A list of all type parameters present on all raw types.
	 */
	public List<TypeVariable<?>> getAllTypeParameters() {
		return getRawTypes().stream().map(ParameterizedTypes::getAllTypeParameters)
				.flatMap(Collection::stream).collect(Collectors.toList());
	}

	/**
	 * <p>
	 * A mapping of type variables to type argument instantiations for this class,
	 * for each type variable returned by {@link #getAllTypeParameters()} . Type
	 * arguments are as those given by {@link #resolveSupertypeParameters(Class)}
	 * invoked on each of {@link #getRawTypes()}.
	 * <p>
	 * For type tokens over a single parameterized type, this method will return
	 * the equivalent of an invocation of
	 * {@link ParameterizedTypes#getAllTypeArguments(ParameterizedType)} on that
	 * type.
	 * 
	 * @return A mapping of all type parameters present on all raw types, to their
	 *         appropriate argument parameterizations according to this type.
	 */
	public Map<TypeVariable<?>, Type> getAllTypeArguments() {
		return getRawTypes()
				.stream()
				.flatMap(
						t -> ParameterizedTypes
								.getAllTypeArguments(
										(ParameterizedType) resolveSupertypeParameters(t).getType())
								.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Try to find an instantiation of the given type variable within the context
	 * provided by the type hierarchy of the type described by this TypeToken.
	 * 
	 * @param type
	 *          The type variable of which we wish to determine an instantiation.
	 * @return A TypeToken over the instantiation of the given type parameter, if
	 *         one exists, otherwise a TypeToken over the given parameter.
	 */
	public Type resolveTypeArgument(TypeVariable<?> type) {
		return getInternalResolver().resolveTypeVariable(getRawType(), type);
	}

	/**
	 * Resolve the type argument which instantiates the given
	 * {@link TypeParameter} in the context of the type of this {@link TypeToken}.
	 * 
	 * @param <U>
	 *          The type parameter whose instantiation we wish to determine.
	 * @param typeParameter
	 *          The type parameter whose instantiation we wish to determine.
	 * @return The proper instantiation of the given parameter, if one exists,
	 *         otherwise a type token over the {@link TypeVariable} itself.
	 */
	@SuppressWarnings("unchecked")
	public <U> TypeToken<U> resolveTypeArgument(TypeParameter<U> typeParameter) {
		return (TypeToken<U>) resolveType(getInternalResolver()
				.resolveTypeVariable(getRawType(), typeParameter.getType()));
	}

	/**
	 * <p>
	 * Substitute all occurrences of a TypeParameter instance mentioned by this
	 * type with a specific type satisfying the bounds of that parameter.
	 * 
	 * As an example, the following method could be used to derive instances of
	 * TypeToken over different parameterizations of {@code List<?>} at runtime.
	 * 
	 * <pre>
	 * <code>
	 * public List&lt;T&gt; getList(TypeToken&lt;T&gt; clazz)} {
	 * 	 return new TypeToken&lt;T&gt;()} {}.withTypeArgument(new TypeParameter&lt;T&gt;() {}, clazz);
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param <V>
	 *          The parameter to make a substitution for.
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
		return (TypeToken<T>) withBoundsFrom(argument).withTypeArgument(
				parameter.getType(), argument.getType());
	}

	/**
	 * As with {@link TypeToken#withTypeArgument(TypeParameter, TypeToken)}.
	 * 
	 * @param <V>
	 *          The parameter to make a substitution for.
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
		return new TypeToken<>(new Resolver(getResolver().getBounds()),
				new TypeSubstitution().where(parameter, argument).resolve(getType()));
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
				.map(m -> Invokable.over((Constructor<T>) m, this))
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

		return methodStream.filter(filter).map(m -> {
			Invokable<T, ?> invokable = Invokable.over(m, this);
			return invokable;
		}).collect(Collectors.toSet());
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
	 * @param argument
	 *          The first argument type for this invocation.
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ? extends T> resolveConstructorOverload(
			Type argument, Type... arguments) {
		return resolveConstructorOverload(Stream
				.concat(Arrays.asList(argument).stream(), Arrays.stream(arguments))
				.map(TypeToken::over).collect(Collectors.toList()));
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
			TypeToken<?>... arguments) {
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
			List<? extends TypeToken<?>> arguments) {
		Set<? extends Invokable<? super T, ? extends T>> candidates = Invokable
				.resolveApplicableInvokables(getConstructors(), arguments);

		if (candidates.isEmpty())
			throw new IllegalArgumentException(
					"Cannot find any applicable constructor in '" + this
							+ "' for arguments '" + arguments + "'.");

		return Invokable.resolveMostSpecificInvokable(candidates);
	}

	/**
	 * Resolve which method invocation matches the given name and argument list,
	 * according to the Java 8 method overload resolution rules.
	 * 
	 * @param name
	 *          The name of the method.
	 * @param argument
	 *          The first argument type for this invocation.
	 * @param arguments
	 *          The list of argument types for this invocation.
	 * @return An {@link Invokable} object wrapping the resolved {@link Method},
	 *         with bounds on any generic type parameters derived from the
	 *         argument types.
	 */
	public Invokable<? super T, ?> resolveMethodOverload(String name,
			Type argument, Type... arguments) {
		return resolveMethodOverload(
				name,
				Stream
						.concat(Arrays.asList(argument).stream(), Arrays.stream(arguments))
						.map(TypeToken::over).collect(Collectors.toList()));
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
			TypeToken<?>... arguments) {
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
			List<? extends TypeToken<?>> arguments) {
		Set<? extends Invokable<? super T, ? extends Object>> candidates = getMethods(m -> m
				.getName().equals(name));

		if (candidates.isEmpty())
			throw new IllegalArgumentException("Cannot find any method '" + name
					+ "' in '" + this + "' for arguments '" + arguments + "'.");

		candidates = Invokable.resolveApplicableInvokables(candidates, arguments);

		return Invokable.resolveMostSpecificInvokable(candidates);
	}

	/**
	 * This method will attempt to substitute any inference variables mentioned by
	 * this type with their instantiations, if instantiations are available, and
	 * return a TypeToken over the resulting type.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	public TypeToken<T> resolve() {
		return new TypeToken<>(getInternalResolver(), resolveType());
	}

	/**
	 * Derive a new {@link TypeToken} by capturing all inference variables
	 * mentioned by this {@link TypeToken}.
	 * 
	 * @return The newly derived {@link TypeToken}
	 */
	public TypeToken<T> captureInferenceVariables() {
		Resolver resolver = getResolver();

		TypeVariableCapture.captureInferenceVariables(resolver.getBounds()
				.getInferenceVariablesMentionedBy(resolver.resolveType(getType())),
				resolver.getBounds());

		return withBounds(resolver.getBounds());
	}

	/**
	 * This method will attempt to infer the actual type represented by this
	 * TypeToken, which means the types of any inference variables mentioned will
	 * be inferred and substituted. The receiver TypeToken instance will not be
	 * changed.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	public TypeToken<T> infer() {
		Resolver resolver = getResolver();

		Type type = resolveType();

		resolver.infer(resolver.getBounds().getInferenceVariablesMentionedBy(type));

		return new TypeToken<>(resolver, resolver.resolveType(type));
	}

	private Type resolveType() {
		return getInternalResolver().resolveType(getType());
	}

	/**
	 * Determine whether this {@link TypeToken} represents a proper type.
	 * 
	 * @return True if the type is proper, false otherwise.
	 */
	public boolean isProper() {
		return getInternalResolver().getBounds().isProperType(type);
	}

	/**
	 * Determine which inference variables are mentioned by the type of this
	 * {@link TypeToken}.
	 * 
	 * @return A set of all the inference variables which are contained within the
	 *         bound set backing this {@link TypeToken} and which are mentioned by
	 *         its type.
	 */
	public Set<InferenceVariable> getInferenceVariablesMentioned() {
		return getInternalResolver().getBounds().getInferenceVariablesMentionedBy(
				type);
	}

	/**
	 * Determine which inference variables are dependencies of those mentioned by
	 * the type of this {@link TypeToken}.
	 * 
	 * @return A set of all the dependencies of the inference variables which are
	 *         contained within the bound set backing this {@link TypeToken} and
	 *         which are mentioned by its type.
	 */
	public Set<InferenceVariable> getRemainingInferenceVariableDependencies() {
		return getInferenceVariablesMentioned()
				.stream()
				.flatMap(
						d -> getInternalResolver().getBounds().getBoundsOn(d)
								.getRemainingDependencies().stream())
				.collect(Collectors.toSet());
	}

	/**
	 * Determine which inference variables are dependencies of those mentioned by
	 * the type of this {@link TypeToken}.
	 * 
	 * @return A set of all the dependencies of the inference variables which are
	 *         contained within the bound set backing this {@link TypeToken} and
	 *         which are mentioned by its type.
	 */
	public Set<InferenceVariable> getInferenceVariableDependencies() {
		return getInferenceVariablesMentioned()
				.stream()
				.flatMap(
						d -> getInternalResolver().getBounds().getBoundsOn(d)
								.getDependencies().stream()).collect(Collectors.toSet());
	}
}
