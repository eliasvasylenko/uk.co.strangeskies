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
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.AnnotatedParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.AnnotatedParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;
import static uk.co.strangeskies.reflection.AnnotatedTypes.wrapImpl;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.WildcardTypes.hasLowerBound;
import static uk.co.strangeskies.reflection.WildcardTypes.hasUpperBound;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map.Entry;

import uk.co.strangeskies.text.grammar.Grammar;
import uk.co.strangeskies.text.grammar.Parser;
import uk.co.strangeskies.text.grammar.Rule;
import uk.co.strangeskies.text.grammar.RuleBuilder;
import uk.co.strangeskies.text.grammar.Symbol;
import uk.co.strangeskies.text.grammar.Variable;

/**
 * A parser for {@link AnnotatedType}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class AnnotatedTypeGrammar {
  private final Grammar grammar;
  private final TypeGrammar typeGrammar;
  private final AnnotationGrammar annotationGrammar;

  private final Variable<AnnotatedType> annotatedRawType;
  private final Variable<AnnotatedArrayType> annotatedArrayType;
  private final Variable<AnnotatedParameterizedType> annotatedParameterizedType;
  private final Variable<AnnotatedArrayType> annotatedGenericArrayType;
  private final Variable<AnnotatedType> annotatedClassOrArrayType;
  private final Variable<AnnotatedWildcardType> annotatedWildcardType;
  private final Variable<AnnotatedType> annotatedType;

  public AnnotatedTypeGrammar(Imports imports) {
    this(new TypeGrammar(imports));
  }

  private AnnotatedTypeGrammar(TypeGrammar typeParser) {
    this(typeParser, new AnnotationGrammar(typeParser));
  }

  public AnnotatedTypeGrammar(TypeGrammar typeGrammar, AnnotationGrammar annotationGrammar) {
    this.typeGrammar = typeGrammar;
    this.annotationGrammar = annotationGrammar;

    Symbol<Class<?>> rawType = typeGrammar.rawType().symbol();
    Symbol<List<Annotation>> annotations = annotationGrammar.annotationList().symbol();

    annotatedRawType = new Variable<>("annotated-raw-type");
    annotatedArrayType = new Variable<>("annotated-array-type");
    annotatedParameterizedType = new Variable<>("annotated-parameterized-type");
    annotatedGenericArrayType = new Variable<>("annotated-generic-array-type");
    annotatedWildcardType = new Variable<>("annotated-wildcard-type");
    annotatedClassOrArrayType = new Variable<>("annotated-class-or-array-type");
    annotatedType = new Variable<>("annotated-type");

    Grammar grammar = null; // TODO

    this.grammar = grammar
        .withRule(createRawTypeRule(rawType, annotations))
        .withRule(createParameterizedTypeRule())
        .withRules(createWildcardTypeRules(annotations));
  }

  private List<Rule> createWildcardTypeRules(Symbol<List<Annotation>> annotations) {
    return asList(

        new RuleBuilder<>(annotatedWildcardType)
            .matchingOutput(o -> hasUpperBound((WildcardType) o.getType()))
            .producing(annotations, string("?").then("extends"), annotatedClassOrArrayType)
            .input(
                in -> wildcardExtending(
                    in.get(annotations),
                    in.getAll(annotatedClassOrArrayType).collect(toList())))
            .output(
                out -> value -> out
                    .put(annotations, asList(value.getAnnotations()))
                    .putAll(annotatedClassOrArrayType, value.getAnnotatedUpperBounds())),

        new RuleBuilder<>(annotatedWildcardType)
            .matchingOutput(o -> hasLowerBound((WildcardType) o.getType()))
            .producing(annotations, string("?").then("super"), annotatedClassOrArrayType)
            .input(
                in -> wildcardSuper(
                    in.get(annotations),
                    in.getAll(annotatedClassOrArrayType).collect(toList())))
            .output(
                out -> value -> out
                    .put(annotations, asList(value.getAnnotations()))
                    .putAll(annotatedClassOrArrayType, value.getAnnotatedLowerBounds())),

        new RuleBuilder<>(annotatedWildcardType)
            .matchingOutput(
                o -> !hasUpperBound((WildcardType) o.getType())
                    && !hasLowerBound((WildcardType) o.getType()))
            .producing(annotations, string("?"))
            .input(in -> wildcard(in.get(annotations)))
            .output(out -> value -> out.put(annotations, asList(value.getAnnotations()))));
  }

  private Rule createParameterizedTypeRule() {
    return new RuleBuilder<>(annotatedParameterizedType)
        .producing(
            annotatedRawType,
            string("<"),
            annotatedType.then(",").repeated(),
            annotatedType,
            string(">"))
        .input(
            in -> parameterize(
                in.get(annotatedRawType),
                in.getAll(annotatedType).collect(toList())))
        .output(
            out -> value -> out
                .put(
                    annotatedRawType,
                    annotated(getErasedType(value.getType()), asList(value.getAnnotations())))
                .putAll(
                    annotatedType,
                    getAllTypeArguments(value).map(Entry::getValue).collect(toList())));
  }

  private Rule createRawTypeRule(Symbol<Class<?>> rawType, Symbol<List<Annotation>> annotations) {
    return new RuleBuilder<>(annotatedRawType)
        .matchingOutput(t -> t.getType() instanceof Class<?>)
        .producing(annotations, rawType)
        .input(in -> annotated(in.get(rawType), in.get(annotations)))
        .output(
            out -> value -> out
                .put(rawType, (Class<?>) value.getType())
                .put(annotations, asList(value.getAnnotations())));
  }

  public Grammar getGrammar() {
    return grammar;
  }

  public Imports getImports() {
    return typeGrammar.getImports();
  }

  /**
   * A parser for annotated raw class types.
   * 
   * @return The annotated raw type of the parsed type name
   */
  public Parser<AnnotatedType> rawType() {
    return grammar.forSymbol(annotatedRawType);
  }

  /**
   * A parser for an annotated class type, which may be parameterized.
   * 
   * @return The type of the expressed name, and the given parameterization where
   *         appropriate
   */
  public Parser<AnnotatedArrayType> arrayType() {
    return grammar.forSymbol(annotatedArrayType);
  }

  /**
   * A parser for an annotated wildcard type.
   * 
   * @return The type of the expressed wildcard type
   */
  public Parser<AnnotatedWildcardType> wildcardType() {
    return grammar.forSymbol(annotatedWildcardType);
  }

  /**
   * A parser for an annotated class type or wildcard type.
   * 
   * @return The annotated type of the expressed type
   */
  public Parser<AnnotatedType> type() {
    return grammar.forSymbol(annotatedType);
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
   * @return A canonical string representation of the given type.
   */
  public String toString(AnnotatedType annotatedType) {
    return wrapImpl(annotatedType).toString(getImports());
  }
}