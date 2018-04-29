/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.text.EscapeFormatter;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of annotated
 * type may be found in {@link AnnotatedWildcardTypes},
 * {@link AnnotatedParameterizedTypes}, and {@link AnnotatedArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class Annotations {
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
   * and package imports allow the names of some classes to be output without full
   * package qualification.
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

    getModifiedProperties(annotation).forEach(property -> {
      annotationProperties
          .add(
              new StringBuilder()
                  .append(property.name())
                  .append(" = ")
                  .append(toPropertyString(property.value(), imports))
                  .toString());
    });

    if (!annotationProperties.isEmpty()) {
      builder
          .append("(")
          .append(annotationProperties.stream().collect(Collectors.joining(", ")))
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
              Arrays
                  .stream((Object[]) object)
                  .map(Annotations::toPropertyString)
                  .collect(Collectors.joining(", ")))
          .append(" }");
    } else {
      StringBuilder builder = new StringBuilder();

      if (String.class.isInstance(object)) {
        builder.append('"').append(EscapeFormatter.java().escape(object.toString())).append('"');

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
        builder.append(Types.toString((Class<?>) object, imports)).append(".class");

      } else {
        builder.append(object.toString());
      }

      return builder;
    }
  }

  static Map<Method, Object> sanitizeProperties(
      Class<? extends Annotation> annotationClass,
      Map<String, Object> properties) {
    properties = new HashMap<>(properties);
    Map<Method, Object> castProperties = new HashMap<>();

    for (Method method : annotationClass.getDeclaredMethods()) {
      Object propertyValue = properties.remove(method.getName());
      if (propertyValue != null) {
        try {
          propertyValue = Types.assign(propertyValue, method.getReturnType());
        } catch (ReflectionException e) {
          Object finalValue = propertyValue;
          throw new ReflectionException(
              REFLECTION_PROPERTIES
                  .invalidAnnotationValue(annotationClass, method.getName(), finalValue),
              e);
        }

        castProperties.put(method, propertyValue);
      } else {
        castProperties.put(method, method.getDefaultValue());
      }
    }

    if (!properties.isEmpty()) {
      Set<String> finalValues = properties.keySet();
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invalidAnnotationProperties(annotationClass, finalValues));
    }

    return castProperties;
  }

  public static Stream<AnnotationProperty> getProperties(Annotation annotation) {
    return Arrays.stream(annotation.annotationType().getDeclaredMethods()).map(propertyMethod -> {
      propertyMethod.setAccessible(true);

      String name = propertyMethod.getName();
      Object value;
      try {
        value = propertyMethod.invoke(annotation);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new IllegalArgumentException(e);
      }

      return new AnnotationProperty(name, value);
    });
  }

  public static Stream<AnnotationProperty> getModifiedProperties(Annotation annotation) {
    return Arrays
        .stream(annotation.annotationType().getDeclaredMethods())
        .flatMap(propertyMethod -> {
          propertyMethod.setAccessible(true);

          String name = propertyMethod.getName();
          Object value;
          try {
            value = propertyMethod.invoke(annotation);
          } catch (
              IllegalAccessException
              | IllegalArgumentException
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

          if (equal) {
            return Stream.empty();
          } else {
            return Stream.of(new AnnotationProperty(name, value));
          }
        });
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

  public static <T extends Annotation> T from(
      Class<T> annotationClass,
      AnnotationProperty... properties) {
    return from(annotationClass, asList(properties));
  }

  public static <T extends Annotation> T from(
      Class<T> annotationClass,
      Collection<? extends AnnotationProperty> properties) {
    Map<String, Object> propertyMap = new HashMap<>();
    for (AnnotationProperty property : properties) {
      propertyMap.put(property.name(), property.value());
    }
    return from(annotationClass, propertyMap);
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
  public static <T extends Annotation> T from(
      Class<T> annotationClass,
      Map<String, Object> properties) {
    Map<Method, Object> castProperties = sanitizeProperties(annotationClass, properties);

    @SuppressWarnings("unchecked")
    T proxy = (T) Proxy
        .newProxyInstance(
            annotationClass.getClassLoader(),
            new Class[] { annotationClass },
            new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("annotationType")
                    && method.getParameterTypes().length == 0) {
                  return annotationClass;
                } else if (method.getName().equals("equals")
                    && Arrays.equals(method.getParameterTypes(), new Class<?>[] { Object.class })) {
                  /*
                   * Check equality
                   */
                  for (Method propertyMethod : annotationClass.getDeclaredMethods()) {
                    Object value = castProperties.get(propertyMethod);
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
                  return true;

                } else if (method.getName().equals("hashCode")
                    && method.getParameterTypes().length == 0) {
                  /*
                   * Generate hash code
                   */
                  int hashCode = 0;
                  for (Method propertyMethod : annotationClass.getDeclaredMethods()) {
                    Object value = castProperties.get(propertyMethod);
                    int nameHash = propertyMethod.getName().hashCode() * 127;

                    if (propertyMethod.getReturnType().isArray()) {
                      hashCode += nameHash ^ Arrays.hashCode((Object[]) value);
                    } else {
                      hashCode += nameHash ^ value.hashCode();
                    }
                  }
                  return hashCode;

                } else if (method.getName().equals("toString")
                    && method.getParameterTypes().length == 0) {
                  return Annotations.toString((Annotation) proxy);

                } else if (method.getDeclaringClass().equals(annotationClass)) {
                  return castProperties.get(method);

                } else {
                  return method.invoke(proxy, args);
                }
              }
            });
    return proxy;
  }
}
