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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of annotated
 * type may be found in {@link AnnotatedWildcardTypes},
 * {@link AnnotatedParameterizedTypes}, and {@link AnnotatedArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypes {
	static class AnnotatedTypeImpl implements AnnotatedType {
		private final Type type;
		private final Map<Class<? extends Annotation>, Annotation> annotations;

		public AnnotatedTypeImpl(Type type,
				Collection<? extends Annotation> annotations) {
			this.type = type;
			this.annotations = new LinkedHashMap<>();
			for (Annotation annotation : annotations)
				this.annotations.put(annotation.annotationType(), annotation);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return (T) annotations.get(annotationClass);
		}

		@Override
		public final Annotation[] getAnnotations() {
			return annotations.values().toArray(new Annotation[annotations.size()]);
		}

		@Override
		public final Annotation[] getDeclaredAnnotations() {
			return getAnnotations();
		}

		@Override
		public Type getType() {
			return type;
		}
	}

	private AnnotatedTypes() {}

	/**
	 * Transform an array of {@link Type}s into a new array of
	 * {@link AnnotatedType}s, according to the behaviour of {@link #over(Type)}
	 * applied to element of the array.
	 * 
	 * @param types
	 *          The array of types to transform.
	 * @return A new array of unannotated {@link AnnotatedType} instances.
	 */
	public static AnnotatedType[] over(Type... types) {
		return Arrays.stream(types).map(AnnotatedTypes::over)
				.toArray(AnnotatedType[]::new);
	}

	/**
	 * Transform a collection of {@link Type}s into a new list of
	 * {@link AnnotatedType}s, according to the behaviour of {@link #over(Type)}
	 * applied to element of the collection.
	 * 
	 * @param types
	 *          The collection of types to transform.
	 * @return A new list of unannotated {@link AnnotatedType} instances.
	 */
	public static List<AnnotatedType> over(Collection<? extends Type> types) {
		return types.stream().map(AnnotatedTypes::over)
				.collect(Collectors.toList());
	}

	/**
	 * Derive a representation of a given type according the appropriate class of
	 * {@link AnnotatedType}.
	 * 
	 * @param type
	 *          The type for which we wish to derive the annotated form.
	 * @return An {@link AnnotatedType} instance of the appropriate class over the
	 *         given type containing no annotations.
	 */
	public static AnnotatedType over(Type type) {
		return over(type, Collections.emptySet());
	}

	/**
	 * Derive a representation of a given type according the appropriate class of
	 * {@link AnnotatedType}, and with the given annotations.
	 * 
	 * @param type
	 *          The type for which we wish to derive the annotated form.
	 * @param annotations
	 *          The annotations we wish for the annotated type to contain.
	 * @return An {@link AnnotatedType} instance of the appropriate class over the
	 *         given type containing the given annotations.
	 */
	public static AnnotatedType over(Type type, Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Derive a representation of a given type according the appropriate class of
	 * {@link AnnotatedType}, and with the given annotations.
	 * 
	 * @param type
	 *          The type for which we wish to derive the annotated form.
	 * @param annotations
	 *          The annotations we wish for the annotated type to contain.
	 * @return An {@link AnnotatedType} instance of the appropriate class over the
	 *         given type containing the given annotations.
	 */
	public static AnnotatedType over(Type type, Collection<Annotation> annotations) {
		if (type instanceof ParameterizedType) {
			return AnnotatedParameterizedTypes.over((ParameterizedType) type,
					annotations);
		} else if (type instanceof WildcardType) {
			return AnnotatedWildcardTypes.over((WildcardType) type, annotations);
		} else if (type instanceof GenericArrayType) {
			return AnnotatedArrayTypes.over((GenericArrayType) type, annotations);
		} else if (type instanceof Class && ((Class<?>) type).isArray()) {
			return AnnotatedArrayTypes.over((Class<?>) type, annotations);
		} else {
			return new AnnotatedTypeImpl(type, annotations);
		}
	}

	/**
	 * Give a canonical String representation of a given annotated type, which is
	 * intended to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * 
	 * @param type
	 *          The type of which we wish to determine a string representation.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(AnnotatedType type) {
		// TODO
		return null;
	}

	/**
	 * Create an AnnotatedType instance from a parsed String.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @return The type described by the String.
	 */
	public static AnnotatedType fromString(String typeString) {
		// TODO
		return null;
	}
}
