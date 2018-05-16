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

import static uk.co.strangeskies.text.grammar.RuleBuilder.buildRuleFor;
import static uk.co.strangeskies.text.grammar.Symbol.regex;
import static uk.co.strangeskies.text.grammar.Symbol.string;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.text.grammar.Grammar;
import uk.co.strangeskies.text.grammar.Variable;

/**
 * A parser for {@link Annotation}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class AnnotationParser {
  private final Grammar grammar;

  private final Variable<Annotation> annotation;
  private final Variable<List<Annotation>> annotationList;
  private final Variable<Map<String, Object>> propertyMap;
  private final Variable<AnnotationProperty> property;
  private final Variable<Object> propertyValue;

  public AnnotationParser(Imports imports) {
    this(new TypeParser(imports));
  }

  public AnnotationParser(TypeParser typeParser) {
    grammar = null; // TODO

    propertyValue = new Variable<>("property-value");

    grammar = grammar
        .withRule(
            buildRuleFor(propertyValue)
                .producing(string("\""), regex("[a-zA-Z0-9_!]*"), string("\"")))
        .withRule(
            buildRuleFor(propertyValue)
                .producing(regex("[0-9]*\\.[0-9]+"),
                string("d")
                .transform(Double::parseDouble))
        .withRule(
            buildRuleFor(propertyValue)
                .producing("[0-9]*\\.[0-9]+")
                .append("f")
                .transform(Float::parseFloat))
        .withRule(
            buildRuleFor(propertyValue).producing("[0-9]+").append("l").transform(Long::parseLong))
        .withRule(
            buildRuleFor(propertyValue)
                .producing("[0-9]+")
                .append("i")
                .transform(Integer::parseInt))
        .withRule(
            buildRuleFor(propertyValue).producing("[0-9]*\\.[0-9]+").transform(Double::parseDouble))
        .withRule(buildRuleFor(propertyValue).producing("[0-9]+").transform(Integer::parseInt));

    property = Parser
        .matching("[_a-zA-Z][_a-zA-Z0-9]*")
        .append("\\s*=\\s*")
        .appendTransform(propertyValue, (s, t) -> new AnnotationProperty(s, t));

    propertyMap = Parser
        .proxy(this::getPropertyMap)
        .prepend("\\s*,\\s*")
        .orElse(HashMap::new)
        .prepend(property, (m, p) -> m.put(p.name(), p.value()))
        .orElse(HashMap::new);

    annotation = typeParser
        .rawType()
        .prepend("@")
        .<Class<? extends Annotation>>transform(t -> t.asSubclass(Annotation.class))
        .appendTransform(
            propertyMap.prepend("\\(\\s*").append("\\s*\\)").orElse(Collections::emptyMap),
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
  public Parser<AnnotationProperty> getProperty() {
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