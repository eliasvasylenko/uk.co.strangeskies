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

import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.text.parsing.Parser;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A parser for {@link Type}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class TypeParser {
  private final Imports imports;
  private final Parser<Class<?>> rawType;

  private final Parser<Type> classOrArrayType;
  private final Parser<WildcardType> wildcardType;
  private final Parser<Type> typeParameter;

  public TypeParser(Imports imports) {
    this.imports = imports;

    rawType = Parser
        .matching("[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*")
        .transform(imports::getNamedClass);

    classOrArrayType = rawType
        .transform(Type.class::cast)
        .tryAppendTransform(
            Parser
                .list(Parser.proxy(this::type), "\\s*,\\s*")
                .prepend("\\s*<\\s*")
                .append("\\s*>\\s*"),
            (t, p) -> ParameterizedTypes.parameterize((Class<?>) t, p))
        .appendTransform(
            Parser.list(Parser.matching("\\s*\\[\\s*\\]"), "\\s*").prepend("\\s*"),
            (t, l) -> {
              t = arrayFromComponent(t, l.size());
              return t;
            });

    wildcardType = Parser
        .matching("\\s*\\?\\s*extends(?![_a-zA-Z0-9])\\s*")
        .appendTransform(
            Parser.list(classOrArrayType, "\\s*\\&\\s*"),
            (s, t) -> WildcardTypes.wildcardExtending(t))
        .orElse(
            Parser
                .matching("\\s*\\?\\s*super(?![_a-zA-Z0-9])\\s*")
                .appendTransform(
                    Parser.list(classOrArrayType, "\\s*\\&\\s*"),
                    (s, t) -> WildcardTypes.wildcardSuper(t)))
        .orElse(Parser.matching("\\s*\\?").transform(s -> WildcardTypes.wildcard()));

    typeParameter = classOrArrayType.orElse(wildcardType.transform(Type.class::cast));
  }

  public Imports getImports() {
    return imports;
  }

  /**
   * A parser for raw class types.
   * 
   * @return The raw type of the parsed type name
   */
  public Parser<Class<?>> rawType() {
    return rawType;
  }

  /**
   * A parser for a class type, which may be parameterized.
   * 
   * @return The type of the expressed name, and the given parameterization where
   *         appropriate
   */
  public Parser<Type> classType() {
    return classOrArrayType;
  }

  /**
   * A parser for a wildcard type.
   * 
   * @return The type of the expressed wildcard type
   */
  public Parser<WildcardType> wildcardType() {
    return wildcardType;
  }

  /**
   * A parser for a class type or wildcard type.
   * 
   * @return The annotated type of the expressed type
   */
  public Parser<Type> type() {
    return typeParameter;
  }

  /**
   * Give a canonical String representation of a given type, which is intended to
   * be more easily human-readable than implementations of
   * {@link Object#toString()} for certain implementations of {@link Type}.
   * 
   * @param type
   *          The type for which we wish to determine a string representation.
   * @return A canonical string representation of the given type.
   */
  public String toString(Type type) {
    return toString(type, new Isomorphism());
  }

  /**
   * Give a canonical String representation of a given type, which is intended to
   * be more easily human-readable than implementations of
   * {@link Object#toString()} for certain implementations of {@link Type}.
   * Provided class and package imports allow the names of some classes to be
   * output without full package qualification.
   * 
   * @param type
   *          the type for which we wish to determine a string representation
   * @param isomorphism
   *          a type to string isomorphic mapping to deal with recursion
   * @return A canonical string representation of the given type.
   */
  public String toString(Type type, Isomorphism isomorphism) {
    if (type == null) {
      return Objects.toString(null);
    } else if (type instanceof Class) {
      if (((Class<?>) type).isArray())
        return new StringBuilder(toString(((Class<?>) type).getComponentType()))
            .append("[]")
            .toString();
      else
        return imports.getClassName((Class<?>) type);
    } else if (type instanceof ParameterizedType) {
      return ParameterizedTypes.toString((ParameterizedType) type, imports, isomorphism);
    } else if (type instanceof GenericArrayType) {
      return new StringBuilder(toString(((GenericArrayType) type).getGenericComponentType()))
          .append("[]")
          .toString();
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      StringBuilder builder = new StringBuilder("?");

      appendBounds(
          builder,
          wildcardType.getUpperBounds(),
          wildcardType.getLowerBounds(),
          imports,
          isomorphism);

      return builder.toString();
    } else if (type instanceof TypeVariableCapture) {
      TypeVariableCapture typeVariableCapture = (TypeVariableCapture) type;
      StringBuilder builder = new StringBuilder(typeVariableCapture.getName());

      appendBounds(
          builder,
          typeVariableCapture.getUpperBounds(),
          typeVariableCapture.getLowerBounds(),
          imports,
          isomorphism);

      return builder.toString();
    } else if (type instanceof IntersectionType) {
      return IntersectionTypes.toString((IntersectionType) type, imports, isomorphism);
    } else
      return type.getTypeName();
  }

  String toString(Type[] types, String delimiter, Isomorphism isomorphism) {
    return Arrays
        .stream(types)
        .map(t -> toString(t, isomorphism))
        .collect(Collectors.joining(delimiter));
  }

  private void appendBounds(
      StringBuilder builder,
      Type[] upperBounds,
      Type[] lowerBounds,
      Imports imports,
      Isomorphism isomorphism) {
    if (upperBounds.length > 0 && (upperBounds.length != 1
        || (upperBounds[0] != null && !upperBounds[0].equals(Object.class))))
      builder.append(" extends ").append(toString(upperBounds, " & ", isomorphism));

    if (lowerBounds.length > 0 && !(lowerBounds.length == 1 && lowerBounds[0] == null))
      builder.append(" super ").append(toString(lowerBounds, " & ", isomorphism));
  }
}