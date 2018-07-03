package uk.co.strangeskies.reflection.model.runtime.types;

import java.lang.reflect.AnnotatedArrayType;

import javax.lang.model.type.ArrayType;

/**
 * A specialization of {@link javax.lang.model.type.ArrayType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeArrayType extends RuntimeReferenceType, ArrayType, ReifiableRuntimeType {
  @Override
  AnnotatedArrayType getSource();

  @Override
  ReifiableRuntimeType getComponentType();
}
