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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.Annotations.AnnotationParser;
import uk.co.strangeskies.reflection.Types.TypeParser;
import uk.co.strangeskies.utilities.parser.Parser;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of annotated
 * type may be found in {@link AnnotatedWildcardTypes},
 * {@link AnnotatedParameterizedTypes}, and {@link AnnotatedArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypes {
	private static final AnnotatedTypeParser ANNOTATED_TYPE_PARSER = new AnnotatedTypeParser(
			Imports.empty());

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
			return (getType() == null ? 0 : getType().hashCode()) ^ annotationHash();
		}

		public int annotationHash() {
			int hash = 0;
			for (Annotation annotation : getAnnotations())
				if (annotation != null)
					hash += annotation.hashCode();
			return hash;
		}

		protected static int annotationHash(AnnotatedTypeImpl... annotatedTypes) {
			int hash = annotatedTypes.length;
			for (int i = 0; i < annotatedTypes.length; i++)
				hash ^= annotatedTypes[i].annotationHash();
			return hash;
		}

		@Override
		public final String toString() {
			return toString(Imports.empty());
		}

		public String toString(Imports imports) {
			return new StringBuilder()
					.append(annotationString(imports, annotations.values()))
					.append(Types.toString(type, imports)).toString();
		}

		protected static String annotationString(Imports imports,
				Annotation... annotations) {
			return annotationString(imports, Arrays.asList(annotations));
		}

		protected static String annotationString(Imports imports,
				Collection<? extends Annotation> annotations) {
			if (!annotations.isEmpty()) {
				StringBuilder builder = new StringBuilder();

				for (Annotation annotation : annotations)
					builder.append(Annotations.toString(annotation, imports)).append(" ");

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
		return AnnotatedTypes.wrap(annotatedType).hashCode();
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
		else {
			return annotationEquals(first, second);
		}
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
				AnnotatedType[] firstUpperBounds = ((AnnotatedWildcardType) first)
						.getAnnotatedUpperBounds();
				AnnotatedType[] secondUpperBounds = ((AnnotatedWildcardType) second)
						.getAnnotatedUpperBounds();

				if (firstUpperBounds.length == 0)
					firstUpperBounds = new AnnotatedType[] { AnnotatedTypes
							.over(Object.class) };

				if (secondUpperBounds.length == 0)
					secondUpperBounds = new AnnotatedType[] { AnnotatedTypes
							.over(Object.class) };

				return annotationEquals(
						((AnnotatedWildcardType) first).getAnnotatedLowerBounds(),
						((AnnotatedWildcardType) second).getAnnotatedLowerBounds())
						&& annotationEquals(firstUpperBounds, secondUpperBounds);
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
		Objects.requireNonNull(type);
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

	protected static AnnotatedTypeImpl[] wrapImpl(AnnotatedType... type) {
		return Arrays.stream(type).map(AnnotatedTypes::wrapImpl)
				.toArray(AnnotatedTypeImpl[]::new);
	}

	protected static AnnotatedTypeImpl wrapImpl(AnnotatedType type) {
		if (type instanceof AnnotatedTypeImpl) {
			return (AnnotatedTypeImpl) type;
		} else if (type instanceof AnnotatedParameterizedType) {
			return AnnotatedParameterizedTypes
					.wrapImpl((AnnotatedParameterizedType) type);
		} else if (type instanceof AnnotatedWildcardType) {
			return AnnotatedWildcardTypes.wrapImpl((AnnotatedWildcardType) type);
		} else if (type instanceof AnnotatedArrayType) {
			return AnnotatedArrayTypes.wrapImpl((AnnotatedArrayType) type);
		} else {
			return new AnnotatedTypeImpl(type);
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
		return wrapImpl(type);
	}

	/**
	 * Give a canonical String representation of a given annotated type, which is
	 * intended to be more easily human-readable than implementations of
	 * {@link Object#toString()} for certain implementations of {@link Type}.
	 * Provided class and package imports allow the names of some classes to be
	 * output without full package qualification.
	 * 
	 * @param annotatedType
	 *          The type of which we wish to determine a string representation.
	 * @param imports
	 *          Classes and packages for which full package qualification may be
	 *          omitted from output.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(AnnotatedType annotatedType, Imports imports) {
		return wrapImpl(annotatedType).toString(imports);
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
		return fromString(typeString, Imports.empty());
	}

	/**
	 * Create an AnnotatedType instance from a parsed String. Provided class and
	 * package imports allow the names of some classes to be given without full
	 * package qualification.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @param imports
	 *          Classes and packages for which full package qualification may be
	 *          omitted from input.
	 * @return The type described by the String.
	 */
	public static AnnotatedType fromString(String typeString, Imports imports) {
		return new AnnotatedTypeParser(imports).getClassType().parse(typeString);
	}

	/**
	 * Get the default annotated type parser. All type names will need to be fully
	 * qualified to correctly parse.
	 * 
	 * @return The default annotated type parser
	 */
	public static AnnotatedTypeParser getParser() {
		return ANNOTATED_TYPE_PARSER;
	}

	/**
	 * Get an annotated type parser with knowledge of the given imports. Type
	 * names may omit full qualification if those types are imported according to
	 * the given imports.
	 * 
	 * @param imports
	 *          A list of imports the annotated type parser should be aware of
	 * @return An annotated type parser with knowledge of the given imports
	 */
	public static AnnotatedTypeParser getParser(Imports imports) {
		return new AnnotatedTypeParser(imports);
	}

	/**
	 * A parser for {@link AnnotatedType}s, and various related types.
	 * 
	 * @author Elias N Vasylenko
	 */
	public static class AnnotatedTypeParser {
		private final Parser<AnnotatedType> rawType;
		private final Parser<AnnotatedType> classOrArrayType;
		private final Parser<AnnotatedWildcardType> wildcardType;
		private final Parser<AnnotatedType> typeParameter;
		private final Parser<List<AnnotatedType>> typeList;

		private AnnotatedTypeParser(Imports imports) {
			AnnotationParser annotationParser = Annotations.getParser(imports);
			TypeParser typeParser = Types.getParser(imports);

			rawType = typeParser.getRawType().prependTransform(
					annotationParser.getAnnotationList().append("\\s*")
							.orElse(ArrayList::new), AnnotatedTypes::over);

			classOrArrayType = rawType.tryAppendTransform(
					Parser.list(Parser.proxy(this::getTypeParameter), "\\s*,\\s*")
							.prepend("\\s*<\\s*").append("\\s*>\\s*"),
					AnnotatedParameterizedTypes::from).appendTransform(
					Parser.list(
							annotationParser.getAnnotationList().append("\\s*\\[\\s*\\]"),
							"\\s*").prepend("\\s*"), (t, l) -> {
						for (List<Annotation> annotationList : l)
							t = AnnotatedArrayTypes.fromComponent(t, annotationList);
						return t;
					});

			wildcardType = annotationParser
					.getAnnotationList()
					.append("\\s*\\?\\s*extends(?![_a-zA-Z0-9])\\s*")
					.appendTransform(Parser.list(classOrArrayType, "\\s*\\&\\s*"),
							AnnotatedWildcardTypes::upperBounded)
					.orElse(
							annotationParser
									.getAnnotationList()
									.append("\\s*\\?\\s*super(?![_a-zA-Z0-9])\\s*")
									.appendTransform(
											Parser.list(classOrArrayType, "\\s*\\&\\s*"),
											AnnotatedWildcardTypes::lowerBounded))
					.orElse(
							annotationParser.getAnnotationList().append("\\s*\\?")
									.transform(AnnotatedWildcardTypes::unbounded));

			typeParameter = classOrArrayType.orElse(wildcardType
					.transform(AnnotatedType.class::cast));

			typeList = Parser.list(rawType, "\\s*,\\s*");
		}

		/**
		 * A parser for annotated raw class types.
		 * 
		 * @return The annotated raw type of the parsed type name
		 */
		public Parser<AnnotatedType> getRawType() {
			return rawType;
		}

		/**
		 * A parser for an annotated class type, which may be parameterized.
		 * 
		 * @return The type of the expressed name, and the given parameterization
		 *         where appropriate
		 */
		public Parser<AnnotatedType> getClassType() {
			return classOrArrayType;
		}

		/**
		 * A parser for an annotated wildcard type.
		 * 
		 * @return The type of the expressed wildcard type
		 */
		public Parser<AnnotatedWildcardType> getWildcardType() {
			return wildcardType;
		}

		/**
		 * A parser for a comma delimited list of annotated Java language types.
		 * 
		 * @return A list of {@link AnnotatedType} objects parsed from a given
		 *         string
		 */
		public Parser<List<AnnotatedType>> getTypeList() {
			return typeList;
		}

		/**
		 * A parser for an annotated class type or wildcard type.
		 * 
		 * @return The annotated type of the expressed type
		 */
		public Parser<AnnotatedType> getTypeParameter() {
			return typeParameter;
		}
	}
}
