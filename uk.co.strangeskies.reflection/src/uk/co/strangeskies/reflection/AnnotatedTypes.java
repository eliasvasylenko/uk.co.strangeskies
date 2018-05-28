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

import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of general utility methods relating to annotated types within
 * the Java type system. Utilities related to more specific classes of annotated
 * type may be found in {@link AnnotatedWildcardTypes},
 * {@link AnnotatedParameterizedTypes}, and {@link AnnotatedArrayTypes}.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypes {
  /**
   * An internal interface to add some extra functionality onto annotated types.
   * 
   * @author Elias N Vasylenko
   */
  public static interface AnnotatedTypeInternal extends AnnotatedType {
    /**
     * As {@link #toString()}, but according to a given {@link Imports}.
     * 
     * @param imports
     *          the imports with which to inform qualification omission
     * @return the string representation of the type
     */
    String toString(Imports imports);

    /**
     * @return a hash code over the annotations of the annotated type
     */
    int annotationHash();
  }

  static class AnnotatedTypeImpl implements AnnotatedTypeInternal {
    private Integer annotationHash;

    private final Type type;
    private final AnnotatedType annotatedOwnerType;
    private final Map<Class<? extends Annotation>, Annotation> annotations;

    public AnnotatedTypeImpl(AnnotatedType annotatedType) {
      this(
          AnnotatedTypes.wrapImpl(annotatedType.getAnnotatedOwnerType()),
          annotatedType.getType(),
          Arrays.asList(annotatedType.getAnnotations()));
    }

    public AnnotatedTypeImpl(Type type, Collection<? extends Annotation> annotations) {
      this(getOwnerType(type), type, annotations);
    }

    public AnnotatedTypeImpl(
        AnnotatedType annotatedOwnerType,
        Type type,
        Collection<? extends Annotation> annotations) {
      this.annotatedOwnerType = annotatedOwnerType;
      this.type = type;
      this.annotations = new LinkedHashMap<>();
      for (Annotation annotation : annotations)
        this.annotations.put(annotation.annotationType(), annotation);
    }

    private static AnnotatedType getOwnerType(Type type) {
      if (type instanceof Class<?>)
        return annotated(((Class<?>) type).getDeclaringClass());
      else if (type instanceof ParameterizedType)
        return annotated(((ParameterizedType) type).getOwnerType());
      return null;
    }

    @Override
    public AnnotatedType getAnnotatedOwnerType() {
      return annotatedOwnerType;
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

    @Override
    public synchronized int annotationHash() {
      if (annotationHash == null) {
        annotationHash = 0;

        annotationHash = annotationHashImpl();
      }

      return annotationHash;
    }

    protected int annotationHashImpl() {
      int hash = 0;
      for (Annotation annotation : getAnnotations())
        if (annotation != null)
          hash += annotation.hashCode();

      return hash;
    }

    protected static int annotationHash(AnnotatedTypeInternal... annotatedTypes) {
      int hash = annotatedTypes.length;
      for (int i = 0; i < annotatedTypes.length; i++)
        hash ^= annotatedTypes[i].annotationHash();
      return hash;
    }

    @Override
    public final String toString() {
      return toString(Imports.empty());
    }

    @Override
    public String toString(Imports imports) {
      return new StringBuilder()
          .append(annotationString(imports, annotations.values()))
          .append(new TypeGrammar(imports).toString(type))
          .toString();
    }

    protected static String annotationString(Imports imports, Annotation... annotations) {
      return annotationString(imports, Arrays.asList(annotations));
    }

    protected static String annotationString(
        Imports imports,
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
      return annotationEquals(new Isomorphism(), first, second);
    }
  }

  private static class AnnotatedTypeEquality {
    private AnnotatedType a, b;

    public AnnotatedTypeEquality(AnnotatedType a, AnnotatedType b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AnnotatedTypeEquality))
        return false;

      AnnotatedTypeEquality that = (AnnotatedTypeEquality) obj;
      return (a == that.a && b == that.b) || (a == that.b && b == that.a);
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(a) ^ (System.identityHashCode(b) * 7);
    }
  }

  private static boolean annotationEquals(
      Isomorphism isomorphism,
      AnnotatedType first,
      AnnotatedType second) {
    return isomorphism
        .byEquality()
        .getPartialMapping(new AnnotatedTypeEquality(first, second), (e, partial) -> {
          partial.accept(() -> true);

          if (!new HashSet<>(Arrays.asList(first.getAnnotations()))
              .equals(new HashSet<>(Arrays.asList(second.getAnnotations()))))
            return false;

          if (first instanceof AnnotatedParameterizedType) {
            if (second instanceof AnnotatedParameterizedType) {
              return annotationEquals(
                  isomorphism,
                  ((AnnotatedParameterizedType) first).getAnnotatedActualTypeArguments(),
                  ((AnnotatedParameterizedType) second).getAnnotatedActualTypeArguments());
            } else
              return false;
          } else if (first instanceof AnnotatedArrayType) {
            if (second instanceof AnnotatedArrayType) {
              return annotationEquals(
                  isomorphism,
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
                firstUpperBounds = new AnnotatedType[] { AnnotatedTypes.annotated(Object.class) };

              if (secondUpperBounds.length == 0)
                secondUpperBounds = new AnnotatedType[] { AnnotatedTypes.annotated(Object.class) };

              return annotationEquals(
                  isomorphism,
                  ((AnnotatedWildcardType) first).getAnnotatedLowerBounds(),
                  ((AnnotatedWildcardType) second).getAnnotatedLowerBounds())
                  && annotationEquals(isomorphism, firstUpperBounds, secondUpperBounds);
            } else
              return false;
          } else
            return true;
        });
  }

  private static boolean annotationEquals(
      Isomorphism isomorphism,
      AnnotatedType[] first,
      AnnotatedType[] second) {
    if (first.length != second.length)
      return false;

    for (int i = 0; i < first.length; i++)
      if (!annotationEquals(isomorphism, first[i], second[i]))
        return false;

    return true;
  }

  private AnnotatedTypes() {}

  /**
   * Transform an array of {@link Type}s into a new array of
   * {@link AnnotatedType}s, according to the behaviour of
   * {@link #annotated(Type)} applied to element of the array.
   * 
   * @param types
   *          The array of types to transform.
   * @return A new array of unannotated {@link AnnotatedType} instances.
   */
  public static AnnotatedType[] annotated(Type... types) {
    return annotatedImpl(new Isomorphism(), types);
  }

  /**
   * Transform a collection of {@link Type}s into a new list of
   * {@link AnnotatedType}s, according to the behaviour of
   * {@link #annotated(Type)} applied to element of the collection.
   * 
   * @param types
   *          The collection of types to transform.
   * @return A new list of unannotated {@link AnnotatedType} instances.
   */
  public static List<AnnotatedType> annotated(Collection<? extends Type> types) {
    return types.stream().map(AnnotatedTypes::annotated).collect(Collectors.toList());
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
  public static AnnotatedType annotated(Type type) {
    return annotated(type, Collections.emptySet());
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
  public static AnnotatedType annotated(Type type, Annotation... annotations) {
    Objects.requireNonNull(type);
    return annotated(type, Arrays.asList(annotations));
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
  public static AnnotatedType annotated(Type type, Collection<Annotation> annotations) {
    return annotatedImpl(new Isomorphism(), type, annotations);
  }

  protected static AnnotatedTypeInternal[] annotatedImpl(Isomorphism isomorphism, Type... types) {
    return Arrays
        .stream(types)
        .map(a -> annotatedImpl(isomorphism, a, Collections.emptySet()))
        .toArray(AnnotatedTypeInternal[]::new);
  }

  protected static AnnotatedTypeInternal annotatedImpl(Isomorphism isomorphism, Type type) {
    return annotatedImpl(isomorphism, type, emptySet());
  }

  protected static AnnotatedTypeInternal annotatedImpl(
      Isomorphism isomorphism,
      Type type,
      Collection<Annotation> annotations) {
    if (type instanceof ParameterizedType) {
      return AnnotatedParameterizedTypes
          .overImpl(isomorphism, (ParameterizedType) type, annotations);
    } else if (type instanceof WildcardType) {
      return AnnotatedWildcardTypes.overImpl(isomorphism, (WildcardType) type, annotations);
    } else if (type instanceof GenericArrayType) {
      return AnnotatedArrayTypes.overImpl(isomorphism, (GenericArrayType) type, annotations);
    } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
      return AnnotatedArrayTypes.overImpl(isomorphism, (Class<?>) type, annotations);
    } else if (type instanceof TypeVariable<?>) {
      return AnnotatedTypeVariables.overImpl(isomorphism, (TypeVariable<?>) type, annotations);
    } else if (type instanceof IntersectionType) {
      return annotatedIntersection(isomorphism, (IntersectionType) type, annotations);
    } else if (type == null) {
      return null;
    } else {
      return new AnnotatedTypeImpl(type, annotations);
    }
  }

  private static AnnotatedTypeInternal annotatedIntersection(
      Isomorphism isomorphism,
      IntersectionType intersectionType,
      Collection<Annotation> annotations) {
    if (annotations.isEmpty()) {
      return isomorphism
          .byIdentity()
          .getMapping(
              intersectionType,
              type -> annotatedIntersectionImpl(isomorphism, type, annotations));
    } else {
      return annotatedIntersectionImpl(isomorphism, intersectionType, annotations);
    }
  }

  private static AnnotatedTypeInternal annotatedIntersectionImpl(
      Isomorphism isomorphism,
      IntersectionType type,
      Collection<Annotation> annotations) {
    return new AnnotatedTypeImpl(type, annotations);
  }

  protected static AnnotatedTypeInternal[] wrapImpl(
      Isomorphism isomorphism,
      AnnotatedType... type) {
    return Arrays
        .stream(type)
        .map(t -> wrapImpl(isomorphism, t))
        .toArray(AnnotatedTypeInternal[]::new);
  }

  protected static AnnotatedTypeInternal wrapImpl(AnnotatedType type) {
    return wrapImpl(new Isomorphism(), type);
  }

  protected static AnnotatedTypeInternal wrapImpl(Isomorphism isomorphism, AnnotatedType type) {
    if (type == null) {
      return null;
    } else if (type instanceof AnnotatedTypeInternal) {
      return (AnnotatedTypeInternal) type;
    } else if (type instanceof AnnotatedParameterizedType) {
      return AnnotatedParameterizedTypes.wrapImpl(isomorphism, (AnnotatedParameterizedType) type);
    } else if (type instanceof AnnotatedWildcardType) {
      return AnnotatedWildcardTypes.wrapImpl(isomorphism, (AnnotatedWildcardType) type);
    } else if (type instanceof AnnotatedArrayType) {
      return AnnotatedArrayTypes.wrapImpl(isomorphism, (AnnotatedArrayType) type);
    } else if (type instanceof AnnotatedTypeVariable) {
      return AnnotatedTypeVariables.wrapImpl(isomorphism, (AnnotatedTypeVariable) type);
    } else {
      return isomorphism.byIdentity().getMapping(type, AnnotatedTypeImpl::new);
    }
  }

  /**
   * Re-implement the given annotated type with correctly working
   * {@link Object#hashCode()} and {@link Object#equals(Object)} implementations.
   * 
   * @param type
   *          The annotated type we wish to re-implement.
   * @return An {@link AnnotatedType} instance equal to the given annotated type.
   */
  public static AnnotatedType wrap(AnnotatedType type) {
    return wrapImpl(new Isomorphism(), type);
  }
}
