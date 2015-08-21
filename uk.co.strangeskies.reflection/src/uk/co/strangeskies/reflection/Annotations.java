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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.Types.TypeParser;
import uk.co.strangeskies.utilities.text.StringEscaper;
import uk.co.strangeskies.utilities.text.parser.Parser;
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
				 * For each method declared on the annotation type...
				 */
				boolean equal;
				if (propertyMethod.getReturnType().isArray()) {
					equal = Arrays.equals((Object[]) value, (Object[]) defaultValue);
				} else {
					equal = Objects.equals(value, defaultValue);
				}

				/*
				 * If value is not equal to default, output:
				 */
				if (!equal) {
					annotationProperties.add(new StringBuilder()
							.append(propertyMethod.getName()).append(" = ")
							.append(toPropertyString(value, imports)).toString());
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

	protected static StringBuilder toPropertyString(Object object) {
		return toPropertyString(object, Imports.empty());
	}

	protected static StringBuilder toPropertyString(Object object, Imports imports) {
		if (object.getClass().isArray()) {
			return new StringBuilder()
					.append(" { ")
					.append(
							Arrays.stream((Object[]) object)
									.map(Annotations::toPropertyString)
									.collect(Collectors.joining(", "))).append(" }");
		} else {
			StringBuilder builder = new StringBuilder();

			if (String.class.isInstance(object)) {
				builder.append('"')
						.append(StringEscaper.java().escape(object.toString())).append('"');

			} else if (Double.class.isInstance(object)) {
				String objectString = object.toString();
				builder.append(objectString);
				if (!objectString.contains("."))
					builder.append("d");

			} else if (Float.class.isInstance(object)) {
				builder.append(object.toString()).append("f");

			} else if (Long.class.isInstance(object)) {
				builder.append(object.toString());
				// if (((Long) object).longValue() > Integer.MAX_VALUE)
				builder.append("l");

			} else if (Class.class.isInstance(object)) {
				builder.append(Types.toString((Class<?>) object, imports)).append(
						".class");

			} else {
				builder.append(object.toString());
			}

			return builder;
		}
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
		return new AnnotationParser(imports).getAnnotation().parse(typeString);
	}

	static Map<Method, Object> sanitizeProperties(Class<?> annotationClass,
			Map<String, Object> properties) {
		Map<Method, Object> castProperties = new HashMap<>();

		for (String propertyName : properties.keySet()) {
			Object propertyValue = properties.get(propertyName);
			Method propertyMethod;
			try {
				propertyMethod = annotationClass.getMethod(propertyName);
				propertyMethod.setAccessible(true);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new TypeException("Annotation property does not exist", e);
			}

			try {
				propertyValue = Types.assign(propertyValue,
						propertyMethod.getReturnType());
			} catch (TypeException e) {
				throw new TypeException("Annotation property " + propertyName
						+ " has invalid value " + propertyValue + " of class "
						+ propertyValue.getClass(), e);
			}

			castProperties.put(propertyMethod, propertyValue);
		}

		return castProperties;
	}

	/**
	 * Try to instantiate an instance of a given annotation type, with default
	 * values for any properties.
	 * 
	 * @param <T>
	 *          The type of the annotation to instantiate
	 * @param annotationClass
	 *          The type of the annotation to instantiate
	 * @return A new annotation of the given type and properties
	 */
	public static <T extends Annotation> T from(Class<T> annotationClass) {
		return from(annotationClass, new HashMap<>());
	}

	/**
	 * Instantiate an instance of a given annotation type, with the given mapping
	 * from properties to their values.
	 * 
	 * @param <T>
	 *          The type of the annotation to instantiate
	 * @param annotationClass
	 *          The type of the annotation to instantiate
	 * @param properties
	 *          A mapping from names of properties on the annotation to values
	 * @return A new annotation of the given type and properties
	 */
	public static <T extends Annotation> T from(Class<T> annotationClass,
			Map<String, Object> properties) {
		Map<Method, Object> castProperties = sanitizeProperties(annotationClass,
				properties);

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
							if (castProperties.containsKey(method)) {
								return castProperties.get(method);
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

	/**
	 * Get the default annotation parser. All type names will need to be fully
	 * qualified to correctly parse.
	 * 
	 * @return The default annotation parser
	 */
	public static AnnotationParser getParser() {
		return ANNOTATION_PARSER;
	}

	/**
	 * Get an annotation parser with knowledge of the given imports. Type names
	 * may omit full qualification if those types are imported according to the
	 * given imports.
	 * 
	 * @param imports
	 *          A list of imports the annotation parser should be aware of
	 * @return An annotation parser with knowledge of the given imports
	 */
	public static AnnotationParser getParser(Imports imports) {
		return new AnnotationParser(imports);
	}

	/**
	 * A parser for {@link Annotation}s, and various related types.
	 * 
	 * @author Elias N Vasylenko
	 */
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
							Parser.matching("[0-9]*\\.[0-9]+").append("d")
									.transform(Double::parseDouble))
					.orElse(
							Parser.matching("[0-9]*\\.[0-9]+").append("f")
									.transform(Float::parseFloat))
					.orElse(
							Parser.matching("[0-9]+").append("l").transform(Long::parseLong))
					.orElse(
							Parser.matching("[0-9]+").append("i")
									.transform(Integer::parseInt))
					.orElse(
							Parser.matching("[0-9]*\\.[0-9]+").transform(Double::parseDouble))
					.orElse(Parser.matching("[0-9]+").transform(Integer::parseInt));

			property = Parser.matching("[_a-zA-Z][_a-zA-Z0-9]*").append("\\s*=\\s*")
					.appendTransform(propertyValue, (s, t) -> new Pair<>(s, t));

			propertyMap = Parser.proxy(this::getPropertyMap).prepend("\\s*,\\s*")
					.orElse(HashMap::new)
					.prepend(property, (m, p) -> m.put(p.getLeft(), p.getRight()))
					.orElse(HashMap::new);

			annotation = typeParser
					.getRawType()
					.prepend("@")
					.<Class<? extends Annotation>> transform(
							t -> t.asSubclass(Annotation.class))
					.appendTransform(
							propertyMap.prepend("\\(\\s*").append("\\s*\\)")
									.orElse(Collections::emptyMap),
							(a, m) -> Annotations.from(a, m));

			annotationList = Parser.list(annotation, "\\s*");
		}

		/**
		 * A parser for the properties of an annotation.
		 * 
		 * @return A mapping from property names to parsed values
		 */
		public Parser<Map<String, Object>> getPropertyMap() {
			return propertyMap;
		}

		/**
		 * A parser for a property of an annotation, as a key, value pair.
		 * 
		 * @return A pair representing the properties name and value
		 */
		public Parser<Pair<String, Object>> getProperty() {
			return property;
		}

		/**
		 * A parser for the value of a property of an annotation
		 * 
		 * @return An object of a valid type for an annotation
		 */
		public Parser<Object> getPropertyValue() {
			return propertyValue;
		}

		/**
		 * A parser for a Java language annotation.
		 * 
		 * @return An {@link Annotation} object parsed from a given string
		 */
		public Parser<Annotation> getAnnotation() {
			return annotation;
		}

		/**
		 * A parser for a whitespace delimited list of Java language annotations.
		 * 
		 * @return A list of {@link Annotation} objects parsed from a given string
		 */
		public Parser<List<Annotation>> getAnnotationList() {
			return annotationList;
		}
	}
}
