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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.Types.TypeParser;
import uk.co.strangeskies.utilities.parser.Parser;
import uk.co.strangeskies.utilities.tuples.Pair;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of annotated
 * type may be found in {@link AnnotatedWildcardTypes},
 * {@link AnnotatedParameterizedTypes}, and {@link AnnotatedArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class Annotations {
	private static final AnnotationParser ANNOTATION_PARSER = new AnnotationParser(
			Imports.empty());

	/**
	 * Give a canonical String representation of a given annotation.
	 * 
	 * @param annotation
	 *          The annotation of which we wish to determine a string
	 *          representation.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(Annotation annotation) {
		return toString(annotation, Imports.empty());
	}

	/**
	 * Give a canonical String representation of a given annotation.Provided class
	 * and package imports allow the names of some classes to be output without
	 * full package qualification.
	 * 
	 * @param annotation
	 *          The annotation of which we wish to determine a string
	 *          representation.
	 * @param imports
	 *          Classes and packages for which full package qualification may be
	 *          omitted from output.
	 * @return A canonical string representation of the given type.
	 */
	public static String toString(Annotation annotation, Imports imports) {
		StringBuilder builder = new StringBuilder("@");

		Class<?> annotationType = annotation.annotationType();
		builder.append(imports.getClassName(annotationType));

		List<String> annotationProperties = new ArrayList<>();

		for (Method propertyMethod : annotationType.getMethods()) {
			if (propertyMethod.getDeclaringClass().equals(annotationType)) {
				propertyMethod.setAccessible(true);

				Object value;
				try {
					value = propertyMethod.invoke(annotation);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
				Object defaultValue = propertyMethod.getDefaultValue();

				/*
				 * For each method declared on the annotation type:
				 */
				if (propertyMethod.getReturnType().isArray()) {
					if (!Arrays.equals((Object[]) value, (Object[]) defaultValue)) {
						/*
						 * Print an array if it is different from the default value
						 */
						annotationProperties.add(new StringBuilder()
								.append(propertyMethod.getName()).append(" = ")
								.append(Arrays.toString((Object[]) value)).toString());
					}
				} else if (!Objects.equals(value, defaultValue)) {
					/*
					 * Print an object if it is different from the default value
					 */
					annotationProperties.add(new StringBuilder()
							.append(propertyMethod.getName()).append(" = ").append(value)
							.toString());
				}
			}
		}

		if (!annotationProperties.isEmpty()) {
			builder
					.append("(")
					.append(
							annotationProperties.stream().collect(Collectors.joining(", ")))
					.append(")");
		}

		return builder.toString();
	}

	/**
	 * Create an Annotation instance from a parsed String.
	 * 
	 * @param typeString
	 *          The String to parse.
	 * @return The type described by the String.
	 */
	public static Annotation fromString(String typeString) {
		return fromString(typeString, Imports.empty());
	}

	/**
	 * Create an Annotation instance from a parsed String. Provided class and
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
	public static Annotation fromString(String typeString, Imports imports) {
		return new AnnotationParser(imports).parse(typeString);
	}

	public static <T extends Annotation> T annotationInstance(
			Class<T> annotationClass, Map<String, Object> properties) {
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(annotationClass.getClassLoader(),
				new Class[] { annotationClass }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						if (method.equals(Annotation.class.getMethod("annotationType"))) {
							return annotationClass;
						} else if (method.getName().equals("equals")
								&& Arrays.equals(method.getParameterTypes(),
										new Class<?>[] { Object.class })) {
							/*
							 * Check equality
							 */
							for (Method propertyMethod : annotationClass.getMethods()) {
								if (propertyMethod.getDeclaringClass().equals(annotationClass)) {
									Object value = propertyMethod.invoke(proxy);
									Object otherValue = propertyMethod.invoke(args[0]);

									/*
									 * For each method declared on the annotation type:
									 */
									if (propertyMethod.getReturnType().isArray()) {
										if (!Arrays.equals((Object[]) value, (Object[]) otherValue)) {
											/*
											 * If it is an array, all elements must be equal
											 */
											return false;
										}
									} else if (!Objects.equals(value, otherValue)) {
										/*
										 * Else the objects must be equal
										 */
										return false;
									}
								}
							}
							return true;
						} else if (method.getName().equals("hashCode")
								&& method.getParameterTypes().length == 0) {
							/*
							 * Generate hash code
							 */
							return 0;
						} else if (method.getName().equals("toString")
								&& method.getParameterTypes().length == 0) {
							return Annotations.toString((Annotation) proxy);
						} else if (method.getDeclaringClass().equals(annotationClass)) {
							if (properties.containsKey(method.getName())) {
								return properties.get(method.getName());
							} else {
								return method.getDefaultValue();
							}
						} else {
							return method.invoke(proxy, args);
						}
					}
				});
		return proxy;
	}

	public static AnnotationParser getParser() {
		return ANNOTATION_PARSER;
	}

	public static AnnotationParser getParser(Imports imports) {
		return new AnnotationParser(imports);
	}

	public static class AnnotationParser {
		private final Parser<Annotation> annotation;
		private final Parser<List<Annotation>> annotationList;
		private final Parser<Map<String, Object>> propertyMap;
		private final Parser<Pair<String, Object>> property;
		private final Parser<Object> propertyValue;

		private AnnotationParser(Imports imports) {
			TypeParser typeParser = Types.getParser(imports);

			propertyValue = Parser
					.matching("[a-zA-Z0-9_!]*")
					.transform(Object.class::cast)
					.prepend("\"")
					.append("\"")
					.orElse(
							Parser.matching("[0-9]*\\.[0-9]+").transform(Double::parseDouble))
					.orElse(Parser.matching("[0-9]+").transform(Long::parseLong));

			property = Parser.matching("[_a-zA-Z][_a-zA-Z0-9]*").append("\\s*=\\s*")
					.appendTransform(propertyValue, (s, t) -> new Pair<>(s, t));

			propertyMap = Parser.proxy(this::getPropertyMap).prepend("\\s*,\\s*")
					.orElse(HashMap::new)
					.prepend(property, (m, p) -> m.put(p.getLeft(), p.getRight()))
					.orElse(HashMap::new);

			annotation = typeParser
					.getRawType()
					.prepend("@")
					.transform(
							t -> (Class<? extends Annotation>) t.asSubclass(Annotation.class))
					.appendTransform(
							propertyMap.prepend("\\(\\s*").append("\\s*\\)")
									.orElse(Collections::emptyMap),
							Annotations::annotationInstance);

			annotationList = Parser.list(annotation, "\\s*");
		}

		public Parser<Map<String, Object>> getPropertyMap() {
			return propertyMap;
		}

		public Parser<Pair<String, Object>> getProperty() {
			return property;
		}

		public Parser<Object> getPropertyValue() {
			return propertyValue;
		}

		public Parser<Annotation> getAnnotation() {
			return annotation;
		}

		public Parser<List<Annotation>> getAnnotationList() {
			return annotationList;
		}

		public Annotation parse(String literal) {
			return annotation.parse(literal);
		}
	}
}
