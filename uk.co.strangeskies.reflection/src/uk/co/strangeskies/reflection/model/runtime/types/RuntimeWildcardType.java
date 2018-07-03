package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.WildcardType;

/**
 * A specialization of {@link javax.lang.model.type.WildcardType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeWildcardType extends RuntimeTypeMirror, WildcardType, ReifiableRuntimeType {
  @Override
  RuntimeTypeMirror getExtendsBound();

  @Override
  RuntimeTypeMirror getSuperBound();
}
