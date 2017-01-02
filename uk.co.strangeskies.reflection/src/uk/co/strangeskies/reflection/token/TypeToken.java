/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.Methods.findMethod;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.token.ExecutableToken.overConstructor;
import static uk.co.strangeskies.reflection.token.ExecutableToken.overMethod;
import static uk.co.strangeskies.reflection.token.ExecutableTokenQuery.executableQuery;
import static uk.co.strangeskies.reflection.token.FieldTokenQuery.fieldQuery;

import java.lang.annotation.Annotation;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.InferenceVariableBounds;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.TypeVariableCapture;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.WildcardTypes;
import uk.co.strangeskies.utilities.DeepCopyable;
import uk.co.strangeskies.utilities.Isomorphism;
import uk.co.strangeskies.utilities.tuple.Pair;

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
 * parameterized with the type it reflects over when used as intended.
 * 
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *            This is the type which the TypeToken object references.
 */
public class TypeToken<T> implements DeepCopyable<TypeToken<T>>, ReifiedToken<TypeToken<T>> {
	/**
	 * Treatment of wildcards for {@link TypeToken}s created over parameterized
	 * types.
	 * 
	 * @author Elias N Vasylenko
	 */
	public enum Wildcards {
		/**
		 * Wildcards will be left alone, though capture may be necessary for
		 * incorporation into backing {@link TypeResolver}, as wildcards alone
		 * do not always fully specify valid bounds.
		 */
		RETAIN(Retain.class),

		/**
		 * Wildcards should be substituted with inference variables, with
		 * appropriate bounds incorporated based on both type variable bounds
		 * and wildcard bounds.
		 */
		INFER(Infer.class),

		/**
		 * Wildcards should be substituted with fresh
		 * {@link TypeVariableCapture} instances, as per
		 * {@link TypeVariableCapture#captureWildcardArguments(ParameterizedType)}
		 * .
		 */
		CAPTURE(Capture.class);

		private final Annotation annotation;

		private Wildcards(Class<? extends Annotation> annotationClass) {
			this.annotation = Annotations.from(annotationClass);
		}

		/**
		 * @return An instance of the annotation which describes this behavior
		 *         for capture of type literals.
		 */
		public Annotation getAnnotation() {
			return annotation;
		}
	}

	/**
	 * Specifies behavior of wildcards. If the annotated type is a wildcard
	 * type, it will behave according to {@link Wildcards#RETAIN}, and if it is
	 * a parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring
	 * types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Retain {
	}

	/**
	 * Specifies behavior of wildcards. If the annotated type is a wildcard
	 * type, it will behave according to {@link Wildcards#INFER}, and if it is a
	 * parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring
	 * types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Infer {
	}

	/**
	 * Specifies behavior of wildcards. If the annotated type is a wildcard
	 * type, it will behave according to {@link Wildcards#CAPTURE}, and if it is
	 * a parameterized type, this rule will apply to its parameters instead.
	 * Annotations on wildcards directly override annotations on declaring
	 * types.
	 * 
	 * @author Elias N Vasylenko
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Capture {
	}

	private static final TypeToken<?> NULL_TYPE_TOKEN = new TypeToken<>(emptyBoundSet(), (AnnotatedType) null,
			(Type) null);

	private final BoundSet bounds;

	private final Type type;
	private final AnnotatedType declaration;

	protected TypeToken() {
		declaration = resolveAnnotatedSuperclassParameter();

		Pair<BoundSet, Type> resolvedType = incorporateAnnotatedType(declaration);
		this.type = resolvedType.getRight();
		this.bounds = resolvedType.getLeft();
	}

	private TypeToken(BoundSet bounds, AnnotatedType annotatedType) {
		Objects.requireNonNull(annotatedType);

		declaration = AnnotatedTypes.wrap(annotatedType);

		Pair<BoundSet, Type> resolvedType;
		if (bounds == null)
			resolvedType = incorporateAnnotatedType(declaration);
		else
			resolvedType = incorporateAnnotatedType(new TypeResolver(bounds), declaration);

		this.type = resolvedType.getRight();
		this.bounds = resolvedType.getLeft();
	}

	private TypeToken(AnnotatedType annotatedType) {
		this(null, annotatedType);
	}

	protected TypeToken(Type type) {
		this(type, Wildcards.RETAIN);
	}

	private TypeToken(Type type, Wildcards wildcards) {
		this(null, type, wildcards);
	}

	private TypeToken(BoundSet bounds, Type type, Wildcards wildcards) {
		this(bounds, AnnotatedTypes.annotated(type, wildcards.getAnnotation()));
	}

	/*
	 * Warning: This is a dangerous constructor to use without a thorough
	 * understanding of the potential consequences.
	 */
	private TypeToken(BoundSet bounds, AnnotatedType declaration, Type type) {
		this.declaration = declaration;
		this.bounds = bounds;
		this.type = type;
	}

	/*
	 * Warning: This is a dangerous constructor to use without a thorough
	 * understanding of the potential consequences.
	 */
	private TypeToken(BoundSet bounds, Type type) {
		this.declaration = AnnotatedTypes.annotated(type);
		this.bounds = bounds;
		this.type = type;
	}

	private AnnotatedType resolveAnnotatedSuperclassParameter() {
		Class<?> subclass = getClass();

		AnnotatedType type;

		final Map<TypeVariable<?>, AnnotatedType> resolvedParameters = new HashMap<>();
		final AnnotatedTypeSubstitution resolvedParameterSubstitution = new AnnotatedTypeSubstitution()
				.where(t -> t instanceof AnnotatedTypeVariable, t -> resolvedParameters.get(t.getType()));

		do {
			AnnotatedType annotatedType = subclass.getAnnotatedSuperclass();
			annotatedType = AnnotatedTypes.wrap(annotatedType);

			if (annotatedType instanceof AnnotatedParameterizedType) {
				if (!resolvedParameters.isEmpty()) {
					type = resolvedParameterSubstitution.resolve(annotatedType);
				} else
					type = annotatedType;

				resolvedParameters.clear();
				AnnotatedParameterizedTypes.getAllTypeArguments((AnnotatedParameterizedType) type)
						.forEach(e -> resolvedParameters.put(e.getKey(), e.getValue()));

				Annotation defaultAnnotation = getWildcardsAnnotation(annotatedType);
				if (defaultAnnotation != null) {
					Iterator<Map.Entry<TypeVariable<?>, AnnotatedType>> parameterIterator = resolvedParameters
							.entrySet().iterator();
					while (parameterIterator.hasNext()) {
						Map.Entry<TypeVariable<?>, AnnotatedType> parameter = parameterIterator.next();

						Annotation givenAnnotation = getWildcardsAnnotation(parameter.getValue());
						if (givenAnnotation == null) {
							parameter.setValue(
									AnnotatedTypes.annotated(parameter.getValue().getType(), defaultAnnotation));
						}
					}
				}
			} else {
				type = annotatedType;

				resolvedParameters.clear();
			}

			subclass = subclass.getSuperclass();
		} while (!subclass.equals(TypeToken.class));

		return resolvedParameters.values().iterator().next();
	}

	private Annotation getWildcardsAnnotation(AnnotatedType type) {
		Annotation annotation = type.getAnnotation(Retain.class);
		if (annotation == null)
			annotation = type.getAnnotation(Capture.class);
		if (annotation == null)
			annotation = type.getAnnotation(Infer.class);

		return annotation;
	}

	private static Pair<BoundSet, Type> incorporateAnnotatedType(TypeResolver resolver, AnnotatedType annotatedType) {
		Type type = substituteAnnotatedWildcards(new Isomorphism(), annotatedType, resolver);

		return new Pair<>(resolver.getBounds(), type);
	}

	private static Pair<BoundSet, Type> incorporateAnnotatedType(AnnotatedType annotatedType) {
		return incorporateAnnotatedType(new TypeResolver(), annotatedType);
	}

	private static Type substituteAnnotatedWildcards(Isomorphism isomorphism, AnnotatedType annotatedType,
			TypeResolver resolver) {
		Wildcards behavior = annotatedType.isAnnotationPresent(Retain.class) ? Wildcards.RETAIN
				: annotatedType.isAnnotationPresent(Infer.class) ? Wildcards.INFER
						: annotatedType.isAnnotationPresent(Capture.class) ? Wildcards.CAPTURE : Wildcards.RETAIN;

		if (annotatedType instanceof AnnotatedParameterizedType) {
			return substituteAnnotatedWildcardsForParameterizedType(isomorphism, behavior,
					(AnnotatedParameterizedType) annotatedType, resolver);
		} else if (annotatedType instanceof AnnotatedWildcardType) {
			return substituteAnnotatedWildcardsForWildcardType(isomorphism, behavior,
					(AnnotatedWildcardType) annotatedType, resolver);
		} else if (annotatedType instanceof AnnotatedArrayType) {
			return arrayFromComponent(substituteAnnotatedWildcards(isomorphism,
					((AnnotatedArrayType) annotatedType).getAnnotatedGenericComponentType(), resolver));
		} else {
			return annotatedType.getType();
		}
	}

	private static ParameterizedType substituteAnnotatedWildcardsForParameterizedType(Isomorphism isomorphism,
			Wildcards behavior, AnnotatedParameterizedType annotatedType, TypeResolver resolver) {
		return isomorphism.byIdentity().getProxiedMapping(annotatedType, ParameterizedType.class, t -> {
			/*
			 * Deal with annotations on types mentioned by parameters,
			 * preserving any parameters which are wildcards themselves.
			 */
			Type[] arguments = substituteAnnotatedWildcardsForEach(isomorphism,
					annotatedType.getAnnotatedActualTypeArguments(), resolver);

			/*
			 * Collect all arguments into a mapping from type variables,
			 * including on enclosing types.
			 */
			Map<TypeVariable<?>, Type> allArguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) annotatedType.getType())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			TypeVariable<?>[] parameters = Types.getRawType(annotatedType.getType()).getTypeParameters();
			for (int i = 0; i < arguments.length; i++) {
				allArguments.put(parameters[i], arguments[i]);
			}

			/*
			 * New parameterized type
			 */
			ParameterizedType parameterizedType = ParameterizedTypes
					.parameterizeUnchecked(Types.getRawType(annotatedType.getType()), allArguments::get);
			if (allArguments.values().stream().anyMatch(WildcardType.class::isInstance)) {
				if (behavior == Wildcards.CAPTURE) {
					parameterizedType = TypeVariableCapture.captureWildcardArguments(parameterizedType);
				} else if (behavior == Wildcards.INFER) {
					TypeResolver inferenceResolver = new TypeResolver(resolver.getBounds());
					parameterizedType = inferenceResolver.inferOverTypeArguments(parameterizedType);
					resolver.incorporateBounds(inferenceResolver.getBounds());
				}
			}
			return parameterizedType;
		});
	}

	private static Type substituteAnnotatedWildcardsForWildcardType(Isomorphism isomorphism, Wildcards behavior,
			AnnotatedWildcardType annotatedType, TypeResolver resolver) {
		AnnotatedWildcardType annotatedWildcardType = annotatedType;
		WildcardType wildcardType;

		if (annotatedWildcardType.getAnnotatedLowerBounds().length > 0) {
			wildcardType = WildcardTypes.wildcardSuper(substituteAnnotatedWildcardsForEach(isomorphism,
					annotatedWildcardType.getAnnotatedLowerBounds(), resolver));

		} else if (annotatedWildcardType.getAnnotatedUpperBounds().length > 0) {
			wildcardType = WildcardTypes.wildcardExtending(substituteAnnotatedWildcardsForEach(isomorphism,
					annotatedWildcardType.getAnnotatedUpperBounds(), resolver));

		} else {
			wildcardType = WildcardTypes.wildcard();
		}

		Type type;
		if (behavior != null) {
			switch (behavior) {
			case RETAIN:
				type = wildcardType;
				break;
			case INFER:
				type = resolver.inferOverWildcardType(wildcardType);
				break;
			case CAPTURE:
				type = TypeVariableCapture.captureWildcard(wildcardType);
				break;
			default:
				throw new AssertionError();
			}
		} else {
			type = wildcardType;
		}

		return type;
	}

	private static Type[] substituteAnnotatedWildcardsForEach(Isomorphism isomorphism, AnnotatedType[] annotatedTypes,
			TypeResolver resolver) {
		Type[] types = new Type[annotatedTypes.length];
		for (int i = 0; i < types.length; i++)
			types[i] = substituteAnnotatedWildcards(isomorphism, annotatedTypes[i], resolver);
		return types;
	}

	/**
	 * Create a TypeToken for a raw class.
	 * 
	 * @param <T>
	 *            the type of the new {@link TypeToken}
	 * @param type
	 *            the class to create a TypeToken for
	 * @return a TypeToken over the requested class
	 */
	public static <T> TypeToken<T> overType(Class<T> type) {
		return new TypeToken<>(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type, preserving wildcards where
	 * possible.
	 * 
	 * @param type
	 *            the requested type
	 * @return a TypeToken over the requested type
	 */
	public static TypeToken<?> overType(Type type) {
		return new TypeToken<>(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type, preserving wildcards where
	 * possible.
	 * 
	 * @param bounds
	 *            the set of bounds on any inference variables mentioned in the
	 *            type
	 * @param type
	 *            the requested type
	 * @param wildcards
	 *            how to deal with wildcard parameters on the given type
	 * @return a TypeToken over the requested type
	 */
	public static TypeToken<?> overType(BoundSet bounds, Type type, Wildcards wildcards) {
		return new TypeToken<>(bounds, type, wildcards);
	}

	/**
	 * Create a TypeToken for an arbitrary type, preserving wildcards where
	 * possible.
	 * 
	 * @param type
	 *            the requested type
	 * @return a TypeToken over the requested type
	 */
	public static TypeToken<?> overAnnotatedType(AnnotatedType type) {
		return new TypeToken<>(type);
	}

	/**
	 * Create a TypeToken for an arbitrary type.
	 * 
	 * @param type
	 *            the requested type
	 * @param wildcards
	 *            how to deal with wildcard parameters on the given type
	 * @return a TypeToken over the requested type
	 */
	public static TypeToken<?> overType(Type type, Wildcards wildcards) {
		return new TypeToken<>(type, wildcards);
	}

	/**
	 * Create a TypeToken over the null type.
	 * 
	 * @param <T>
	 *            the target type
	 * @return a TypeToken over the null type
	 */
	@SuppressWarnings("unchecked")
	public static <T> TypeToken<T> overNull() {
		return (TypeToken<T>) NULL_TYPE_TOKEN;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TypeToken && getType().equals(((TypeToken<?>) obj).getType());
	}

	@Override
	public int hashCode() {
		return getType().hashCode();
	}

	@Override
	public TypeToken<T> getThis() {
		return ReifiedToken.super.getThis();
	}

	@Override
	public TypeToken<TypeToken<T>> getThisTypeToken() {
		return new TypeToken<TypeToken<T>>() {
		}.withTypeArgument(new TypeParameter<T>() {
		}, this);
	}

	@Override
	public TypeToken<T> copy() {
		return this;
	}

	@Override
	public TypeToken<T> deepCopy(Isomorphism isomorphism) {
		return new TypeToken<>(bounds.deepCopy(isomorphism), declaration,
				new TypeSubstitution().withIsomorphism(isomorphism).resolve(getType()));
	}

	/**
	 * Derive a new {@link TypeToken} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link TypeToken} will remain unmodified.
	 * 
	 * <p>
	 * All bounds are incorporated if and only if they have the potential to
	 * affect the resolution of inference variables mentioned by this type.
	 * 
	 * @param bounds
	 *            the new bounds to incorporate
	 * @return the newly derived {@link TypeToken}
	 */
	public TypeToken<T> withBounds(BoundSet bounds) {
		return new TypeToken<>(this.bounds.withBounds(bounds), getType());
	}

	/**
	 * Equivalent to the application of {@link TypeToken#overType(Type)} to the
	 * result of {@link Types#fromString(String)}.
	 * 
	 * @param typeString
	 *            the String to parse
	 * @return a TypeToken representing the type described by the String
	 */
	public static TypeToken<?> fromString(String typeString) {
		return overAnnotatedType(AnnotatedTypes.fromString(typeString));
	}

	/**
	 * Equivalent to the application of {@link TypeToken#overType(Type)} to the
	 * result of {@link AnnotatedTypes#fromString(String, Imports)}, with the
	 * given imports.
	 * 
	 * @param typeString
	 *            the String to parse
	 * @param imports
	 *            classes and packages for which full package qualification may
	 *            be omitted from input
	 * @return a TypeToken representing the type described by the string
	 */
	public static TypeToken<?> fromString(String typeString, Imports imports) {
		return overAnnotatedType(AnnotatedTypes.fromString(typeString, imports));
	}

	/**
	 * Equivalent to the application of
	 * {@link AnnotatedTypes#toString(AnnotatedType, Imports)} to
	 * {@link #getAnnotatedDeclaration()}, with the given imports.
	 * 
	 * @param imports
	 *            classes and packages for which full package qualification may
	 *            be omitted from output
	 * @return a string representing the type described by this type token
	 */
	public String toString(Imports imports) {
		return AnnotatedTypes.toString(declaration, imports);
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as an upper bound.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will
	 * give a {@code TypeToken<? extends List<?>>}.
	 * 
	 * @return the TypeToken representing a capture of the wildcard described
	 */
	public TypeToken<? extends T> getWildcardExtending() {
		return getExtending(Wildcards.RETAIN);
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
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will
	 * give a {@code TypeToken<? extends List<?>>}.
	 * 
	 * @param wildcards
	 *            how to deal with the wildcard parameter on the new type
	 * @return the TypeToken representing a capture of the wildcard described
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? extends T> getExtending(Wildcards wildcards) {
		if (wildcards == Wildcards.INFER) {
			TypeResolver resolver = new TypeResolver(this.bounds);
			InferenceVariable inferenceVariable = resolver.inferOverWildcardType(wildcardExtending(getType()));

			return new TypeToken<>(resolver.getBounds(), inferenceVariable);
		} else {
			return (TypeToken<? extends T>) overType(wildcardExtending(getType()), wildcards);
		}
	}

	/**
	 * Create a TypeToken over a wildcard type which has the type represented by
	 * this TypeToken as a lower bound.
	 * 
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will
	 * give a {@code TypeToken<? super List<?>>}.
	 * 
	 * @return the TypeToken representing a capture of the wildcard described
	 */
	public TypeToken<? super T> getWildcardSuper() {
		return getSuper(Wildcards.RETAIN);
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
	 * For example, invoking this method on a {@code TypeToken<List<?>>} will
	 * give a {@code TypeToken<? super List<?>>}.
	 * 
	 * @param wildcards
	 *            how to deal with the wildcard parameter on the new type
	 * @return the TypeToken representing a capture of the wildcard described
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<? super T> getSuper(Wildcards wildcards) {
		if (wildcards == Wildcards.INFER) {
			TypeResolver resolver = new TypeResolver(this.bounds);
			InferenceVariable inferenceVariable = resolver.inferOverWildcardType(wildcardSuper(getType()));
			return new TypeToken<>(resolver.getBounds(), inferenceVariable);
		} else {
			return (TypeToken<? super T>) overType(WildcardTypes.wildcardSuper(getType()), wildcards);
		}
	}

	/**
	 * See {@link Types#getRawType(Type)}.
	 * 
	 * @return the raw type of the type represented by this TypeToken
	 */
	@SuppressWarnings("unchecked")
	public Class<? super T> getRawType() {
		return (Class<? super T>) getRawTypes().findFirst().orElse(Object.class);
	}

	/**
	 * Find the upper bounds of a given type. Unlike
	 * {@link Types#getUpperBounds(Type)} this respects bounds on the inference
	 * variables in this resolver.
	 * 
	 * @return the upper bounds of the type represented by this TypeToken
	 */
	public Stream<Type> getUpperBounds() {
		List<Type> upperBounds = Types.getUpperBounds(getType()).collect(toList());

		for (int i = 0; i < upperBounds.size(); i++) {
			Type upperBound = upperBounds.get(i);

			if (getBounds().containsInferenceVariable(upperBound)) {
				upperBounds.remove(upperBound);

				InferenceVariableBounds bounds = getBounds().getBoundsOn((InferenceVariable) upperBound);
				Stream.concat(bounds.getUpperBounds(), bounds.getEqualities())
						.filter(t -> !getBounds().containsInferenceVariable(t)).forEach(upperBounds::add);
			}
		}

		if (upperBounds.isEmpty())
			upperBounds.add(Object.class);

		return upperBounds.stream();
	}

	/**
	 * Determine the raw types of a given type, accounting for inference
	 * variables which may have instantiations or upper bounds within the
	 * context of this resolver.
	 * 
	 * @return the raw types of the type represented by this TypeToken
	 */
	public Stream<Class<?>> getRawTypes() {
		return getUpperBounds().map(Types::getRawType);
	}

	/**
	 * This method returns a copy of the Resolver backing by this TypeToken.
	 * 
	 * @return A new Resolver object containing whichever bounds have been
	 *         internally derived from the type of this TypeToken.
	 */
	public BoundSet getBounds() {
		return bounds;
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
			return (TypeToken<T>) overType(Types.wrapPrimitive(getRawType()));
		else
			return this;
	}

	/**
	 * If this TypeToken is a wrapper of a primitive type, determine the
	 * unwrapped primitive type.
	 * 
	 * @return The primitive type wrapped by this TypeToken, otherwise this
	 *         TypeToken itself.
	 */
	@SuppressWarnings("unchecked")
	public TypeToken<T> unwrapPrimitive() {
		if (isPrimitiveWrapper())
			return (TypeToken<T>) overType(Types.unwrapPrimitive(getRawType()));
		else
			return this;
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public TypeToken<T> withConstraintTo(Kind kind, Type type) {
		BoundSet bounds = new ConstraintFormula(kind, getType(), type).reduce(getBounds());

		return new TypeToken<>(bounds, resolveType());
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public TypeToken<T> withConstraintTo(Kind kind, TypeToken<?> type) {
		BoundSet bounds = getBounds().withBounds(type.getBounds());
		bounds = new ConstraintFormula(kind, getType(), type.getType()).reduce(bounds);

		return new TypeToken<>(bounds, resolveType());
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public TypeToken<T> withConstraintFrom(Kind kind, Type type) {
		BoundSet bounds = new ConstraintFormula(kind, type, getType()).reduce(getBounds());

		return new TypeToken<>(bounds, resolveType());
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public TypeToken<T> withConstraintFrom(Kind kind, TypeToken<?> type) {
		BoundSet bounds = getBounds().withBounds(type.getBounds());
		bounds = new ConstraintFormula(kind, type.getType(), getType()).reduce(bounds);

		return new TypeToken<>(bounds, resolveType());
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public boolean satisfiesConstraintTo(Kind kind, Type type) {
		try {
			withConstraintTo(kind, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public boolean satisfiesConstraintTo(Kind kind, TypeToken<?> type) {
		try {
			withConstraintTo(kind, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public boolean satisfiesConstraintFrom(Kind kind, Type type) {
		try {
			withConstraintFrom(kind, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Derive a new type from this one, with a constraint between this type and
	 * a given type. The invocation will fail if the constraint cannot be
	 * satisfied. For types which mention inference variables, this constraint
	 * may have an effect on the bounds of those inference variables within the
	 * resulting type.
	 * 
	 * @param type
	 *            the type to constrain against
	 * @param kind
	 *            the kind of the constraint formula
	 * @return a new type token which satisfies the constraint
	 */
	public boolean satisfiesConstraintFrom(Kind kind, TypeToken<?> type) {
		try {
			withConstraintFrom(kind, type);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Determine the super type of this type which is either equal to the given
	 * superclass or a parameterization thereof.
	 * 
	 * @param superclass
	 *            the class of the supertype parameterization we wish to
	 *            determine
	 * @return the supertype of the requested class
	 */
	@SuppressWarnings("unchecked")
	public <U> TypeToken<? extends U> resolveSupertype(Class<U> superclass) {
		Type type = getUpperBounds().filter(t -> Types.isAssignable(t, superclass)).findFirst()
				.orElseThrow(() -> new ReflectionException(p -> p.cannotResolveSupertype(getType(), superclass)));

		return (TypeToken<? extends U>) overType(getBounds(), ParameterizedTypes.resolveSupertype(type, superclass),
				Wildcards.RETAIN);
	}

	/**
	 * Determine the recursive sequence of direct supertypes of this type which
	 * lead to either the given superclass or a parameterization thereof.
	 * 
	 * @param superclass
	 *            the class of the supertype parameterization we wish to
	 *            determine
	 * @return a stream returning the given type and then each direct supertype
	 *         recursively until the given superclass, or a parameterization
	 *         thereof, is reached
	 */
	@SuppressWarnings("unchecked")
	public <U> Stream<TypeToken<? extends U>> resolveSupertypeHierarchy(Class<U> superclass) {
		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * TODO This first part is not good enough. We need to retain every step
		 * in the getUpperBounds() calculation, as it may be recursive
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		Type type = getUpperBounds().filter(t -> Types.isAssignable(t, superclass)).findFirst()
				.orElseThrow(() -> new ReflectionException(p -> p.cannotResolveSupertype(getType(), superclass)));

		Stream<TypeToken<? extends U>> supertypeHierarchy = ParameterizedTypes
				.resolveSupertypeHierarchy(type, superclass)
				.map(t -> (TypeToken<? extends U>) overType(getBounds(), t, Wildcards.RETAIN));

		if (type != this.type) {
			supertypeHierarchy = Stream.concat(Stream.of((TypeToken<? extends U>) this), supertypeHierarchy);
		}

		return supertypeHierarchy;
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
	 *            The parameter to make a substitution for.
	 * @param parameter
	 *            The parameter to make a substitution for.
	 * @param argument
	 *            The argument to substitute for that parameter.
	 * @return A new TypeToken instance over the type resulting from the
	 *         substitution.
	 */
	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter, TypeToken<V> argument) {
		return (TypeToken<T>) withBounds(argument.getBounds()).withTypeArgument(parameter.getType(),
				argument.getType());
	}

	/**
	 * As with {@link TypeToken#withTypeArgument(TypeParameter, TypeToken)}.
	 * 
	 * @param <V>
	 *            The parameter to make a substitution for.
	 * @param parameter
	 *            The parameter to make a substitution for.
	 * @param argument
	 *            The argument to substitute for that parameter.
	 * @return A new TypeToken instance over the type resulting from the
	 *         substitution.
	 */
	@SuppressWarnings("unchecked")
	public <V> TypeToken<T> withTypeArgument(TypeParameter<V> parameter, Class<V> argument) {
		return (TypeToken<T>) withTypeArgument(parameter.getType(), argument);
	}

	private TypeToken<?> withTypeArgument(TypeVariable<?> parameter, Type argument) {
		return new TypeToken<>(getBounds(), new TypeSubstitution().where(parameter, argument).resolve(getType()));
	}

	@SuppressWarnings("unchecked")
	public <U> TypeToken<U> resolveTypeArgument(TypeParameter<U> parameter) {
		if (!(getType() instanceof ParameterizedType)) {
			throw new ReflectionException(p -> p.cannotResolveTypeVariable(parameter.getType(), this));
		} else {
			return (TypeToken<U>) overType(getAllTypeArguments((ParameterizedType) getType())
					.filter(e -> e.getKey().equals(parameter.getType())).findAny()
					.orElseThrow(
							() -> new ReflectionException(p -> p.cannotResolveTypeVariable(parameter.getType(), this)))
					.getValue());
		}
	}

	/**
	 * This method will attempt to substitute any inference variables mentioned
	 * by this type with their instantiations, if instantiations are available,
	 * and return a TypeToken over the resulting type.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	public TypeToken<T> resolve() {
		Type type = resolveType();
		BoundSet bounds = getBounds();

		if (InferenceVariable.isProperType(type)) {
			bounds = emptyBoundSet();
		}

		return new TypeToken<>(bounds, type);
	}

	public TypeToken<?> getEnclosingType() {
		Type enclosingType;

		if (getType() instanceof ParameterizedType) {
			enclosingType = ((ParameterizedType) getType()).getOwnerType();
		} else {
			enclosingType = getRawType().getEnclosingClass();
		}

		if (enclosingType == null) {
			return null;
		}

		return TypeToken.overType(getBounds(), enclosingType, Wildcards.RETAIN);
	}

	/**
	 * This method will attempt to infer the actual type represented by this
	 * TypeToken, which means the types of any inference variables mentioned
	 * will be inferred and substituted. The receiver TypeToken instance will
	 * not be changed.
	 * 
	 * @return A TypeToken with the fully inferred type.
	 */
	public TypeToken<T> infer() {
		TypeResolver resolver = new TypeResolver(getBounds());

		return new TypeToken<>(emptyBoundSet(), resolver.infer(getType()));
	}

	private Type resolveType() {
		return new TypeResolver(getBounds()).resolveType(getType());
	}

	/**
	 * Determine whether this {@link TypeToken} represents a proper type.
	 * 
	 * @return True if the type is proper, false otherwise.
	 */
	public boolean isProper() {
		return InferenceVariable.isProperType(getType());
	}

	/**
	 * Determine which inference variables are mentioned by the type of this
	 * {@link TypeToken}.
	 * 
	 * @return A set of all the inference variables which are contained within
	 *         the bound set backing this {@link TypeToken} and which are
	 *         mentioned by its type.
	 */
	public Stream<InferenceVariable> getInferenceVariablesMentioned() {
		return InferenceVariable.getMentionedBy(getType());
	}

	/**
	 * Determine which inference variables are dependencies of those mentioned
	 * by the type of this {@link TypeToken}.
	 * 
	 * @return A set of all the dependencies of the inference variables which
	 *         are contained within the bound set backing this {@link TypeToken}
	 *         and which are mentioned by its type.
	 */
	public Stream<InferenceVariable> getRemainingInferenceVariableDependencies() {
		return getInferenceVariablesMentioned().flatMap(d -> getBounds().getBoundsOn(d).getRemainingDependencies());
	}

	/**
	 * @return The annotated declaring type of this type token, if one exists,
	 *         else an unannotated representation of the type of this type
	 *         token.
	 */
	public AnnotatedType getAnnotatedDeclaration() {
		return declaration;
	}

	/**
	 * @param object
	 *            an object to cast to this type
	 * @return the given object cast to a reference of this type
	 */
	@SuppressWarnings("unchecked")
	public T cast(Object object) {
		/*
		 * TODO actually test castability ...
		 */
		return (T) object;
	}

	/**
	 * @param type
	 *            the type to determine castability to
	 * @return true if the cast is possible, false otherwise
	 */
	public boolean isCastableTo(TypeToken<?> type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param type
	 *            the type to determine castability from
	 * @return true if the cast is possible, false otherwise
	 */
	public boolean isCastableFrom(TypeToken<?> type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param type
	 *            the type to determine castability to
	 * @return true if the cast is possible, false otherwise
	 */
	public boolean isCastableTo(Type type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param type
	 *            the type to determine castability from
	 * @return true if the cast is possible, false otherwise
	 */
	public boolean isCastableFrom(Type type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Find which fields can be resolved on this type.
	 * 
	 * @return a list of all {@link Field} objects applicable to this type,
	 *         wrapped in {@link FieldToken} instances
	 */
	public FieldTokenQuery<FieldToken<T, ?>, ?> fields() {
		Stream<Field> fields = stream(getRawType().getFields());

		return fieldQuery(fields, f -> FieldToken.overField(f, this));
	}

	/**
	 * Find which fields are declared on this type.
	 * 
	 * @return a list of all {@link Field} objects applicable to this type,
	 *         wrapped in {@link FieldToken} instances
	 */
	public FieldTokenQuery<FieldToken<T, ?>, ?> declaredFields() {
		Stream<Field> fields = stream(getRawType().getDeclaredFields());

		return fieldQuery(fields, f -> FieldToken.overField(f, this));
	}

	/**
	 * Find which constructors can be invoked for this type.
	 * 
	 * @return a list of all {@link Constructor} objects applicable to this
	 *         type, wrapped in {@link ExecutableToken} instances
	 */
	@SuppressWarnings("unchecked")
	public ExecutableTokenQuery<ExecutableToken<Void, T>, ?> constructors() {
		Stream<Constructor<?>> constructors = stream(getRawType().getConstructors());

		return executableQuery(constructors, m -> overConstructor((Constructor<T>) m, this));
	}

	/**
	 * Find which constructors can be invoked for this type.
	 * 
	 * @return a list of all {@link Constructor} objects applicable to this
	 *         type, wrapped in {@link ExecutableToken} instances
	 */
	@SuppressWarnings("unchecked")
	public ExecutableTokenQuery<ExecutableToken<Void, T>, ?> declaredConstructors() {
		Stream<Constructor<?>> constructors = stream(getRawType().getDeclaredConstructors());

		return executableQuery(constructors, m -> overConstructor((Constructor<T>) m, this));
	}

	/**
	 * Find which methods of the given name can be invoked on instances of this
	 * type.
	 * 
	 * @return a list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link ExecutableToken} instances
	 */
	public ExecutableTokenQuery<ExecutableToken<T, ?>, ?> methods() {
		Stream<Method> methodStream = getRawTypes().flatMap(t -> Arrays.stream(t.getMethods()));

		if (getRawTypes().allMatch(Types::isInterface))
			methodStream = Stream.concat(methodStream, Arrays.stream(Object.class.getMethods()));

		methodStream = methodStream.filter(m -> !Modifier.isStatic(m.getModifiers()));

		return executableQuery(methodStream, m -> overMethod(m, this));
	}

	/**
	 * Find which methods can be invoked on this type, whether statically or on
	 * instances.
	 * 
	 * @return a list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link ExecutableToken} instances
	 */
	public ExecutableTokenQuery<ExecutableToken<T, ?>, ?> declaredMethods() {
		Stream<Method> methodStream = stream(getRawType().getDeclaredMethods())
				.filter(m -> !Modifier.isStatic(m.getModifiers()));

		return executableQuery(methodStream, m -> overMethod(m, this));
	}

	@SuppressWarnings("unchecked")
	public ExecutableToken<T, ?> findInterfaceMethod(Consumer<? super T> methodLambda) {
		return getRawTypes().filter(Class::isInterface).flatMap(superType -> {
			try {
				return of(findMethod(superType, (Consumer<Object>) methodLambda));
			} catch (Exception e) {
				return empty();
			}
		}).findAny().map(method -> (ExecutableToken<T, ?>) overMethod(method, this))
				.orElseThrow(() -> new ReflectionException(p -> p.cannotFindMethodOn(getType())));
	}
}
