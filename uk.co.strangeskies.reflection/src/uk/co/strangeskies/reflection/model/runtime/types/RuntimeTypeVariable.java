package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.TypeVariable;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;

/**
 * A specialization of {@link javax.lang.model.type.TypeVariable} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeTypeVariable
    extends RuntimeReferenceType, TypeVariable, ReifiableRuntimeType {
  @Override
  RuntimeElement asElement();

  @Override
  RuntimeTypeMirror getUpperBound();

  @Override
  RuntimeTypeMirror getLowerBound();
}
