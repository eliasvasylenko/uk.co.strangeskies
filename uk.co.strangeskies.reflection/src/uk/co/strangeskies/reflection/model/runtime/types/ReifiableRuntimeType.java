package uk.co.strangeskies.reflection.model.runtime.types;

import java.lang.reflect.AnnotatedType;

/**
 * A specialization of {@link javax.lang.model.type.TypeMirror} which operates
 * over the core reflection API and is directly representable as an
 * {@link AnnotatedType} instance.
 * 
 * @author Elias N Vasylenko
 */
public interface ReifiableRuntimeType extends RuntimeTypeMirror {
  /**
   * @return the annotated type corresponding with the type of the mirror
   */
  AnnotatedType getSource();
}