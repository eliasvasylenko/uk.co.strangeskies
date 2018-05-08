package uk.co.strangeskies.reflection;

import static uk.co.strangeskies.reflection.AnnotatedTypes.wrapImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.text.parsing.Parser;

/**
 * A parser for {@link AnnotatedType}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class AnnotatedTypeParser {
  private final TypeParser typeParser;

  private final Parser<AnnotatedType> rawType;

  private final Parser<AnnotatedType> classOrArrayType;
  private final Parser<AnnotatedWildcardType> wildcardType;
  private final Parser<AnnotatedType> typeParameter;

  public AnnotatedTypeParser(Imports imports) {
    this(new TypeParser(imports));
  }

  private AnnotatedTypeParser(TypeParser typeParser) {
    this(typeParser, new AnnotationParser(typeParser));
  }

  public AnnotatedTypeParser(TypeParser typeParser, AnnotationParser annotationParser) {
    this.typeParser = typeParser;

    rawType = typeParser
        .rawType()
        .prependTransform(
            annotationParser.getAnnotationList().append("\\s*").orElse(ArrayList::new),
            AnnotatedTypes::annotated);

    classOrArrayType = rawType
        .tryAppendTransform(
            Parser
                .list(Parser.proxy(this::type), "\\s*,\\s*")
                .prepend("\\s*<\\s*")
                .append("\\s*>\\s*"),
            AnnotatedParameterizedTypes::parameterize)
        .appendTransform(
            Parser
                .list(annotationParser.getAnnotationList().append("\\s*\\[\\s*\\]"), "\\s*")
                .prepend("\\s*"),
            (t, l) -> {
              for (List<Annotation> annotationList : l)
                t = AnnotatedArrayTypes.arrayFromComponent(t, annotationList);
              return t;
            });

    wildcardType = annotationParser
        .getAnnotationList()
        .append("\\s*\\?\\s*extends(?![_a-zA-Z0-9])\\s*")
        .appendTransform(
            Parser.list(classOrArrayType, "\\s*\\&\\s*"),
            AnnotatedWildcardTypes::wildcardExtending)
        .orElse(
            annotationParser
                .getAnnotationList()
                .append("\\s*\\?\\s*super(?![_a-zA-Z0-9])\\s*")
                .appendTransform(
                    Parser.list(classOrArrayType, "\\s*\\&\\s*"),
                    AnnotatedWildcardTypes::wildcardSuper))
        .orElse(
            annotationParser
                .getAnnotationList()
                .append("\\s*\\?")
                .transform(AnnotatedWildcardTypes::wildcard));

    typeParameter = classOrArrayType.orElse(wildcardType.transform(AnnotatedType.class::cast));
  }

  public Imports getImports() {
    return typeParser.getImports();
  }

  /**
   * A parser for annotated raw class types.
   * 
   * @return The annotated raw type of the parsed type name
   */
  public Parser<AnnotatedType> rawType() {
    return rawType;
  }

  /**
   * A parser for an annotated class type, which may be parameterized.
   * 
   * @return The type of the expressed name, and the given parameterization where
   *         appropriate
   */
  public Parser<AnnotatedType> classType() {
    return classOrArrayType;
  }

  /**
   * A parser for an annotated wildcard type.
   * 
   * @return The type of the expressed wildcard type
   */
  public Parser<AnnotatedWildcardType> wildcardType() {
    return wildcardType;
  }

  /**
   * A parser for an annotated class type or wildcard type.
   * 
   * @return The annotated type of the expressed type
   */
  public Parser<AnnotatedType> type() {
    return typeParameter;
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