package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.text.parsing.Parser;

/**
 * A parser for {@link AnnotatedType}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class AnnotatedTypeParser {
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
    rawType = typeParser
        .rawType()
        .prependTransform(
            annotationParser.getAnnotationList().append("\\s*").orElse(ArrayList::new),
            AnnotatedTypes::annotated);

    classOrArrayType = rawType
        .tryAppendTransform(
            Parser
                .list(Parser.proxy(this::getType), "\\s*,\\s*")
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

  /**
   * A parser for annotated raw class types.
   * 
   * @return The annotated raw type of the parsed type name
   */
  public Parser<AnnotatedType> getRawType() {
    return rawType;
  }

  /**
   * A parser for an annotated class type, which may be parameterized.
   * 
   * @return The type of the expressed name, and the given parameterization where
   *         appropriate
   */
  public Parser<AnnotatedType> getClassType() {
    return classOrArrayType;
  }

  /**
   * A parser for an annotated wildcard type.
   * 
   * @return The type of the expressed wildcard type
   */
  public Parser<AnnotatedWildcardType> getWildcardType() {
    return wildcardType;
  }

  /**
   * A parser for an annotated class type or wildcard type.
   * 
   * @return The annotated type of the expressed type
   */
  public Parser<AnnotatedType> getType() {
    return typeParameter;
  }
}