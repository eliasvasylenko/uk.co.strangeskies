package uk.co.strangeskies.reflection.model.runtime.elements;

import java.lang.reflect.TypeVariable;

import javax.lang.model.element.TypeParameterElement;

/**
 * A specialization of {@link javax.lang.model.element.TypeParameterElement}
 * which operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeTypeParameterElement extends RuntimeElement, TypeParameterElement {
  @Override
  RuntimeElement getGenericElement();

  @Override
  TypeVariable<?> getSource();
}