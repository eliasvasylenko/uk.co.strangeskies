package uk.co.strangeskies.reflection;

import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import uk.co.strangeskies.text.parsing.Parser;

/**
 * A parser for {@link Type}s, and various related types.
 * 
 * @author Elias N Vasylenko
 */
public class TypeParser {
  private final Parser<Class<?>> rawType;

  private final Parser<Type> classOrArrayType;
  private final Parser<WildcardType> wildcardType;
  private final Parser<Type> typeParameter;

  public TypeParser(Imports imports) {
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
}