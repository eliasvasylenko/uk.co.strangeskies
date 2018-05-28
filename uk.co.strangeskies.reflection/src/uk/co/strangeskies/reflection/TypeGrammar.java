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
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.WildcardTypes.hasLowerBound;
import static uk.co.strangeskies.reflection.WildcardTypes.hasUpperBound;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.text.grammar.Grammar;
import uk.co.strangeskies.text.grammar.Parser;
import uk.co.strangeskies.text.grammar.Rule;
import uk.co.strangeskies.text.grammar.RuleBuilder;
import uk.co.strangeskies.text.grammar.Symbol;
import uk.co.strangeskies.text.grammar.Terminal;
import uk.co.strangeskies.text.grammar.Variable;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A parser for {@link Type}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class TypeGrammar {
  private final Grammar grammar;

  private final Imports imports;

  private final Terminal<String> typeName;
  private final Variable<Class<?>> rawType;
  private final Variable<Class<?>> arrayType;
  private final Variable<ParameterizedType> parameterizedType;
  private final Variable<GenericArrayType> genericArrayType;
  private final Variable<Type> classOrArrayType;
  private final Variable<WildcardType> wildcardType;
  private final Variable<Type> type;

  public TypeGrammar(Imports imports) {
    this.imports = imports;

    typeName = Symbol.regex("[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*");
    rawType = new Variable<>("raw-type");
    arrayType = new Variable<>("array-type");
    parameterizedType = new Variable<>("parameterized-type");
    genericArrayType = new Variable<>("generic-array-type");
    wildcardType = new Variable<>("wildcard-type");
    classOrArrayType = new Variable<>("class-or-array-type");
    type = new Variable<>("type");

    Grammar grammar = null; // TODO

    this.grammar = grammar
        .withRule(createRawTypeRule())
        .withRules(createArrayTypeRules())
        .withRule(createParameterizedTypeRule())
        .withRules(createGenericArrayTypeRules())
        .withRules(createClassOrArrayTypeRules())
        .withRules(createWildcardTypeRules())
        .withRules(createTypeRules());
  }

  private List<Rule> createTypeRules() {
    return asList(
        new RuleBuilder<>(type).producingIdentity(classOrArrayType),
        new RuleBuilder<>(type).matchingOutput(WildcardType.class).producingIdentity(wildcardType));
  }

  private List<Rule> createWildcardTypeRules() {
    return asList(

        new RuleBuilder<>(wildcardType)
            .matchingOutput(WildcardTypes::hasUpperBound)
            .producing(string("?").then("extends"), classOrArrayType)
            .input(in -> wildcardExtending(in.get(classOrArrayType)))
            .output(
                out -> value -> out.put(classOrArrayType, intersectionOf(value.getUpperBounds()))),

        new RuleBuilder<>(wildcardType)
            .matchingOutput(WildcardTypes::hasLowerBound)
            .producing(string("?").then("super"), classOrArrayType)
            .input(in -> wildcardSuper(in.get(classOrArrayType)))
            .output(
                out -> value -> out.put(classOrArrayType, intersectionOf(value.getLowerBounds()))),

        new RuleBuilder<>(wildcardType)
            .matchingOutput(t -> !hasUpperBound(t) && !hasLowerBound(t))
            .producing(string("?"))
            .input(in -> wildcard())
            .output(out -> value -> {}));
  }

  private List<Rule> createClassOrArrayTypeRules() {
    return asList(

        new RuleBuilder<>(classOrArrayType)
            .matchingOutput(c -> c instanceof Class<?> && !((Class<?>) c).isArray())
            .<Class<?>>transforming(c -> (Class<?>) c, identity())
            .producingIdentity(rawType),

        new RuleBuilder<>(classOrArrayType)
            .matchingOutput(c -> c instanceof Class<?> && ((Class<?>) c).isArray())
            .<Class<?>>transforming(c -> (Class<?>) c, identity())
            .producingIdentity(arrayType),

        new RuleBuilder<>(classOrArrayType)
            .matchingOutput(ParameterizedType.class)
            .producingIdentity(parameterizedType),

        new RuleBuilder<>(classOrArrayType)
            .matchingOutput(GenericArrayType.class)
            .producingIdentity(genericArrayType));
  }

  private List<Rule> createGenericArrayTypeRules() {
    return asList(

        new RuleBuilder<>(genericArrayType)
            .matchingOutput(g -> g.getGenericComponentType() instanceof ParameterizedType)
            .producing(parameterizedType.then("[").then("]"))
            .input(in -> arrayFromComponent(in.get(parameterizedType)))
            .output(
                out -> value -> out
                    .put(parameterizedType, (ParameterizedType) value.getGenericComponentType())),

        new RuleBuilder<>(genericArrayType)
            .matchingOutput(g -> g.getGenericComponentType() instanceof GenericArrayType)
            .producing(genericArrayType.then("[").then("]"))
            .input(in -> arrayFromComponent(in.get(genericArrayType)))
            .output(
                out -> value -> out
                    .put(genericArrayType, (GenericArrayType) value.getGenericComponentType())));
  }

  private Rule createParameterizedTypeRule() {
    return new RuleBuilder<>(parameterizedType)
        .producing(rawType, string("<"), type.then(",").repeated(), type, string(">"))
        .input(in -> parameterize(in.get(rawType), in.getAll(type).collect(toList())))
        .output(
            out -> value -> out
                .put(rawType, getErasedType(value))
                .putAll(type, getAllTypeArguments(value).map(Entry::getValue).collect(toList())));
  }

  private List<Rule> createArrayTypeRules() {
    return asList(

        new RuleBuilder<>(arrayType)
            .matchingOutput(c -> c.getComponentType().isArray())
            .producing(arrayType.then("[").then("]"))
            .input(in -> arrayFromComponent(in.get(arrayType)))
            .output(out -> value -> out.put(arrayType, value.getComponentType())),

        new RuleBuilder<>(arrayType)
            .matchingOutput(c -> !c.getComponentType().isArray())
            .producing(rawType.then("[").then("]"))
            .input(in -> arrayFromComponent(in.get(rawType)))
            .output(out -> value -> out.put(rawType, value.getComponentType())));
  }

  private Rule createRawTypeRule() {
    return new RuleBuilder<>(rawType)
        .producingSymbol(typeName)
        .input(imports::getNamedClass)
        .output(Class::getName);
  }

  public Grammar getGrammar() {
    return grammar;
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
    return grammar.forSymbol(rawType);
  }

  /**
   * A parser for a class type, which may be parameterized.
   * 
   * @return The type of the expressed name, and the given parameterization where
   *         appropriate
   */
  public Parser<Class<?>> arrayType() {
    return grammar.forSymbol(arrayType);
  }

  /**
   * A parser for a wildcard type.
   * 
   * @return The type of the expressed wildcard type
   */
  public Parser<WildcardType> wildcardType() {
    return grammar.forSymbol(wildcardType);
  }

  /**
   * A parser for a class type or wildcard type.
   * 
   * @return The annotated type of the expressed type
   */
  public Parser<Type> type() {
    return grammar.forSymbol(type);
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