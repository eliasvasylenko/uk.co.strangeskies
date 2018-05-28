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

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.text.grammar.Symbol.regex;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.lang.annotation.Annotation;
import java.util.List;

import uk.co.strangeskies.text.grammar.Grammar;
import uk.co.strangeskies.text.grammar.Parser;
import uk.co.strangeskies.text.grammar.RuleBuilder;
import uk.co.strangeskies.text.grammar.Symbol;
import uk.co.strangeskies.text.grammar.Terminal;
import uk.co.strangeskies.text.grammar.Variable;

/**
 * A parser for {@link Annotation}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class AnnotationGrammar {
  private final Grammar grammar;

  private final Variable<Annotation> annotation;
  private final Variable<List<Annotation>> annotationList;
  private final Variable<AnnotationProperty> property;
  private final Terminal<String> propertyName;
  private final Variable<Object> propertyValue;

  public AnnotationGrammar(Imports imports) {
    this(new TypeGrammar(imports));
  }

  public AnnotationGrammar(TypeGrammar typeParser) {
    Grammar grammar = null; // TODO

    propertyValue = new Variable<>("property-value");
    Terminal<String> stringRegex = regex("[a-zA-Z0-9_!]*");
    grammar = grammar
        .withRule(
            new RuleBuilder<>(propertyValue)
                .matchingOutput(String.class)
                .producing(string("\""), stringRegex, string("\""))
                .input(in -> in.get(stringRegex))
                .output(out -> value -> out.put(stringRegex, value)))
        .withRules(
            new RuleBuilder<>(propertyValue)
                .matchingOutput(Double.class)
                .producing(regex("[0-9]*\\.[0-9]+"), string("d").optionally())
                .input(in -> null)
                .output(out -> value -> {}),
            new RuleBuilder<>(propertyValue)
                .matchingOutput(Float.class)
                .producing(regex("[0-9]*\\.[0-9]+"), string("f"))
                .input(in -> null)
                .output(out -> value -> {}),
            new RuleBuilder<>(propertyValue)
                .matchingOutput(Long.class)
                .producing(regex("[0-9]+"), string("l"))
                .input(in -> null)
                .output(out -> value -> {}),
            new RuleBuilder<>(propertyValue)
                .matchingOutput(Integer.class)
                .producing(regex("[0-9]+"), string("i").optionally())
                .input(in -> null)
                .output(out -> value -> {}));

    property = new Variable<>("property");
    propertyName = regex("[_a-zA-Z][_a-zA-Z0-9]*");
    grammar = grammar
        .withRule(
            new RuleBuilder<>(property)
                .producing(propertyName, string("\\s*=\\s*"), propertyValue)
                .input(in -> new AnnotationProperty(in.get(propertyName), in.get(propertyValue)))
                .output(out -> value -> {
                  out.put(propertyName, value.name());
                  out.put(propertyValue, value.value());
                }));

    grammar = grammar
        .withRulesFor(typeParser.rawType().symbol())
        .importedFrom(typeParser.getGrammar());
    annotation = new Variable<>("annotation");
    Symbol<Class<?>> rawType = typeParser.rawType().symbol();
    grammar = grammar
        .withRule(
            new RuleBuilder<>(annotation)
                .producing(
                    string("@"),
                    rawType,
                    string("(")
                        .then(
                            property.then(",").repeated().then(property).optionally(),
                            string(")"))
                        .optionally())
                .input(
                    in -> Annotations
                        .from(
                            in.get(rawType).asSubclass(Annotation.class),
                            in.getAll(property).collect(toList())))
                .output(
                    out -> value -> out
                        .put(rawType, value.annotationType())
                        .putAll(property, Annotations.getProperties(value).collect(toList()))));

    annotationList = new Variable<>("annotation-list");
    grammar = grammar
        .withRule(
            new RuleBuilder<>(annotationList)
                .producing(annotation.then(",").repeated().then(annotation).optionally())
                .input(in -> in.getAll(annotation).collect(toList()))
                .output(out -> value -> out.putAll(annotation, value)));

    this.grammar = grammar;
  }

  public Grammar getGrammar() {
    return grammar;
  }

  /**
   * A parser for a property of an annotation, as a key, value pair.
   * 
   * @return A pair representing the properties name and value
   */
  public Parser<AnnotationProperty> property() {
    return grammar.forSymbol(property);
  }

  /**
   * A parser for the value of a property of an annotation
   * 
   * @return An object of a valid type for an annotation
   */
  public Parser<Object> propertyValue() {
    return grammar.forSymbol(propertyValue);
  }

  /**
   * A parser for a Java language annotation.
   * 
   * @return An {@link Annotation} object parsed from a given string
   */
  public Parser<Annotation> annotation() {
    return grammar.forSymbol(annotation);
  }

  /**
   * A parser for a whitespace delimited list of Java language annotations.
   * 
   * @return A list of {@link Annotation} objects parsed from a given string
   */
  public Parser<List<Annotation>> annotationList() {
    return grammar.forSymbol(annotationList);
  }
}
