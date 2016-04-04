/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of utility methods relating to annotated parameterised types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedParameterizedTypes {
	private static class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl implements AnnotatedParameterizedType {
		private final AnnotatedTypeImpl[] annotatedTypeArguments;

		public AnnotatedParameterizedTypeImpl(Set<TypeVariable<?>> wrapped,
				AnnotatedParameterizedType annotatedParameterizedType) {
			super(annotatedParameterizedType);

			annotatedTypeArguments = AnnotatedTypes.wrapImpl(wrapped,
					annotatedParameterizedType.getAnnotatedActualTypeArguments());
		}

		public AnnotatedParameterizedTypeImpl(ParameterizedType type, Collection<? extends Annotation> annotations,
				Map<Type, AnnotatedType> annotatedTypes) {
			super(type, annotations);

			annotatedTypeArguments = AnnotatedTypes.overImpl(type.getActualTypeArguments());
		}

		public AnnotatedParameterizedTypeImpl(Class<?> rawType,
				Function<? super TypeVariable<?>, ? extends AnnotatedType> annotatedTypes,
				Collection<? extends Annotation> annotations) {
			super(ParameterizedTypes.uncheckedFrom(rawType, unannotatedTypes(annotatedTypes)), annotations);

			annotatedTypeArguments = AnnotatedTypes.wrapImpl(Arrays.stream(rawType.getTypeParameters()).map(p -> {
				AnnotatedType type = annotatedTypes.apply(p);
				if (type == null)
					return AnnotatedTypes.over(p);
				else
					return type;
			}).toArray(AnnotatedType[]::new));
		}

		/*
		 * This really shouldn't need to be factored out into its own method, but
		 * the JDT was having internal errors in Windows and this fixed it...
		 */
		private static Function<? super TypeVariable<?>, ? extends Type> unannotatedTypes(
				Function<? super TypeVariable<?>, ? extends AnnotatedType> annotatedTypes) {
			return annotatedTypes.andThen(AnnotatedType::getType);
		}

		@Override
		public AnnotatedType[] getAnnotatedActualTypeArguments() {
			return annotatedTypeArguments.clone();
		}

		@Override
		public ParameterizedType getType() {
			return (ParameterizedType) super.getType();
		}

		@Override
		public String toString(Imports imports) {
			StringBuilder builder = new StringBuilder(annotationString(imports, getAnnotations()))
					.append(Types.toString(getType().getRawType(), imports)).append("<");

			builder.append(Arrays.stream(getAnnotatedActualTypeArguments()).map(t -> AnnotatedTypes.toString(t, imports))
					.collect(Collectors.joining(", ")));

			return builder.append(">").toString();
		}

		@Override
		public int annotationHash() {
			return super.annotationHash() ^ annotationHash(annotatedTypeArguments);
		}
	}

	private AnnotatedParameterizedTypes() {}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type, Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type, Collection<Annotation> annotations) {
		return over(type, annotations, new IdentityHashMap<>());
	}

	private static AnnotatedParameterizedType over(ParameterizedType type, Collection<Annotation> annotations,
			Map<Type, AnnotatedType> annotatedTypes) {
		if (annotations.isEmpty() && annotatedTypes.containsKey(type)) {
			return (AnnotatedParameterizedType) annotatedTypes.get(type);
		} else {
			AnnotatedParameterizedType annotatedType = new AnnotatedParameterizedTypeImpl(type, annotations, annotatedTypes);
			if (annotations.isEmpty())
				annotatedTypes.put(type, annotatedType);
			return annotatedType;
		}
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments. {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedParameterizedType from(AnnotatedType rawType,
			Function<? super TypeVariable<?>, ? extends AnnotatedType> arguments) {
		return from((Class<?>) rawType.getType(), arguments, rawType.getAnnotations());
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedParameterizedType from(Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends AnnotatedType> arguments, Annotation... annotations) {
		return new AnnotatedParameterizedTypeImpl(rawType, arguments, Arrays.asList(annotations));
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedType from(AnnotatedType rawType, AnnotatedType... arguments) {
		return from(rawType, Arrays.asList(arguments));
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedType from(AnnotatedType rawType, List<AnnotatedType> arguments) {
		Map<TypeVariable<?>, AnnotatedType> annotatedTypes = new HashMap<>();
		TypeVariable<?>[] typeVariables = ((Class<?>) rawType.getType()).getTypeParameters();

		if (typeVariables.length != arguments.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < arguments.size(); i++)
			annotatedTypes.put(typeVariables[i], arguments.get(i));

		return AnnotatedParameterizedTypes.from((Class<?>) rawType.getType(), annotatedTypes::get,
				rawType.getAnnotations());
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by
	 * {@link ParameterizedTypes#getAllTypeParameters(Class)} applied to the raw
	 * type of this annotated type, to their annotated arguments within the
	 * context of this type.
	 *
	 * @param type
	 *          The type whose generic type arguments we wish to determine.
	 * @return A mapping of all type variables to their arguments in the context
	 *         of the given type.
	 */
	public static Map<TypeVariable<?>, AnnotatedType> getAllTypeArguments(AnnotatedParameterizedType type) {
		AnnotatedType[] arguments = type.getAnnotatedActualTypeArguments();

		Map<TypeVariable<?>, AnnotatedType> allArguments = new HashMap<>();
		TypeVariable<?>[] parameters = Types.getRawType(type.getType()).getTypeParameters();
		for (int i = 0; i < arguments.length; i++)
			allArguments.put(parameters[i], arguments[i]);

		for (Map.Entry<TypeVariable<?>, Type> entry : ParameterizedTypes
				.getAllTypeArguments((ParameterizedType) type.getType()).entrySet())
			allArguments.putIfAbsent(entry.getKey(), AnnotatedTypes.over(entry.getValue()));

		return allArguments;
	}

	protected static AnnotatedParameterizedTypeImpl wrapImpl(AnnotatedParameterizedType type) {
		return wrapImpl(AnnotatedTypes.wrappingVisitedSet(), type);
	}

	protected static AnnotatedParameterizedTypeImpl wrapImpl(Set<TypeVariable<?>> wrapped,
			AnnotatedParameterizedType type) {
		if (type instanceof AnnotatedParameterizedTypeImpl) {
			return (AnnotatedParameterizedTypeImpl) type;
		} else
			return new AnnotatedParameterizedTypeImpl(wrapped, type);
	}

	/**
	 * Wrap an existing annotated parameterized type.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedParameterizedType} which is equal
	 *         to the given type.
	 */
	public static AnnotatedParameterizedType wrap(AnnotatedParameterizedType type) {
		return wrapImpl(type);
	}
}
