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
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

		public AnnotatedTypeImpl(AnnotatedType annotatedType) {
			this(annotatedType.getType(), Arrays.asList(annotatedType
					.getAnnotations()));
		}

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

		@Override
		public boolean equals(Object that) {
			if (that instanceof AnnotatedType)
				return AnnotatedTypes.equals(this, (AnnotatedType) that);
			else
				return false;
		}

		@Override
		public int hashCode() {
			return AnnotatedTypes.hashCode(this);
		}

		@Override
		public String toString() {
			return new StringBuilder().append(annotationString(annotations.values()))
					.append(type.toString()).toString();
		}

		protected static String annotationString(Annotation... annotations) {
			return annotationString(Arrays.asList(annotations));
		}

		protected static String annotationString(
				Collection<? extends Annotation> annotations) {
			if (!annotations.isEmpty()) {
				StringBuilder builder = new StringBuilder();

				for (Annotation annotation : annotations)
					builder.append(annotation).append(" ");

				return builder.toString();
			} else {
				return "";
			}
		}
	}

	/**
	 * A correct hash code implementation for annotated types, since the Java
	 * specification does not require implementors to provide this.
	 * 
	 * @param annotatedType
	 *          The annotated type whose hash code we wish to determine.
	 * @return A hash code for the given annotated type.
	 */
	public static int hashCode(AnnotatedType annotatedType) {
		return annotatedType.getType().hashCode() ^ annotationHash(annotatedType);
	}

	private static int annotationHash(AnnotatedType annotatedType) {
		int hash = new HashSet<>(Arrays.asList(annotatedType.getAnnotations()))
				.hashCode();

		if (annotatedType instanceof AnnotatedParameterizedType) {
			hash ^= annotationHash(((AnnotatedParameterizedType) annotatedType)
					.getAnnotatedActualTypeArguments());
		} else if (annotatedType instanceof AnnotatedArrayType) {
			hash ^= annotationHash(((AnnotatedArrayType) annotatedType)
					.getAnnotatedGenericComponentType());
		} else if (annotatedType instanceof AnnotatedWildcardType) {
			hash ^= annotationHash(((AnnotatedWildcardType) annotatedType)
					.getAnnotatedLowerBounds())
					^ annotationHash(((AnnotatedWildcardType) annotatedType)
							.getAnnotatedUpperBounds());
		}

		return hash;
	}

	private static int annotationHash(AnnotatedType[] annotatedTypes) {
		int hash = annotatedTypes.length;
		for (int i = 0; i < annotatedTypes.length; i++)
			hash ^= annotationHash(annotatedTypes[i]);
		return hash;
	}

	/**
	 * A correct equality implementation for annotated types, since the Java
	 * specification does not require implementors to provide this.
	 * 
	 * @param first
	 *          The first of the two annotated types whose equality we wish to
	 *          determine.
	 * @param second
	 *          The second of the two annotated types whose equality we wish to
	 *          determine.
	 * @return True if the two given annotated types are equal, false otherwise.
	 */
	public static boolean equals(AnnotatedType first, AnnotatedType second) {
		if (first == null)
			return second == null;
		else if (second == null)
			return false;
		else if (!first.getType().equals(second.getType()))
			return false;
		else
			return annotationEquals(first, second);
	}

	private static boolean annotationEquals(AnnotatedType first,
			AnnotatedType second) {
		if (!new HashSet<>(Arrays.asList(first.getAnnotations()))
				.equals(new HashSet<>(Arrays.asList(second.getAnnotations()))))
			return false;

		if (first instanceof AnnotatedParameterizedType) {
			if (second instanceof AnnotatedParameterizedType) {
				return annotationEquals(
						((AnnotatedParameterizedType) first)
								.getAnnotatedActualTypeArguments(),
						((AnnotatedParameterizedType) second)
								.getAnnotatedActualTypeArguments());
			} else
				return false;
		} else if (first instanceof AnnotatedArrayType) {
			if (second instanceof AnnotatedArrayType) {
				return annotationEquals(
						((AnnotatedArrayType) first).getAnnotatedGenericComponentType(),
						((AnnotatedArrayType) second).getAnnotatedGenericComponentType());
			} else
				return false;
		} else if (first instanceof AnnotatedWildcardType) {
			if (second instanceof AnnotatedWildcardType) {
				return annotationEquals(
						((AnnotatedWildcardType) first).getAnnotatedLowerBounds(),
						((AnnotatedWildcardType) second).getAnnotatedLowerBounds())
						&& annotationEquals(
								((AnnotatedWildcardType) first).getAnnotatedUpperBounds(),
								((AnnotatedWildcardType) second).getAnnotatedUpperBounds());
			} else
				return false;
		} else
			return true;
	}

	private static boolean annotationEquals(AnnotatedType[] first,
			AnnotatedType[] second) {
		if (first.length != second.length)
			return false;

		for (int i = 0; i < first.length; i++)
			if (!annotationEquals(first[i], second[i]))
				return false;

		return true;
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
	 * Re-implement the given annotated type with correctly working
	 * {@link Object#hashCode()} and {@link Object#equals(Object)}
	 * implementations.
	 * 
	 * @param type
	 *          The annotated type we wish to re-implement.
	 * @return An {@link AnnotatedType} instance equal to the given annotated
	 *         type.
	 */
	public static AnnotatedType wrap(AnnotatedType type) {
		if (type instanceof AnnotatedParameterizedType) {
			return AnnotatedParameterizedTypes
					.wrap((AnnotatedParameterizedType) type);
		} else if (type instanceof AnnotatedWildcardType) {
			return AnnotatedWildcardTypes.wrap((AnnotatedWildcardType) type);
		} else if (type instanceof AnnotatedArrayType) {
			return AnnotatedArrayTypes.wrap((AnnotatedArrayType) type);
		} else {
			return new AnnotatedTypeImpl(type);
		}
	}

	/**
	 * Give a canonical String representation of a given annotated type, which is
	 * intended to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * 
	 * @param annotatedType
	 *          The type of which we wish to determine a string representation.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(AnnotatedType annotatedType) {
		return wrap(annotatedType).toString();
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
