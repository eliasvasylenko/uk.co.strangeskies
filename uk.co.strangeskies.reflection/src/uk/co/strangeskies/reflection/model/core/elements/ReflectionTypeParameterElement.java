package uk.co.strangeskies.reflection.model.core.elements;

import javax.lang.model.element.TypeParameterElement;

/**
 * A specialization of {@code javax.lang.model.element.TypeParameterElement}
 * being backed by core reflection.
 */
public interface ReflectionTypeParameterElement
    extends ReflectionElement, TypeParameterElement {

  /**
   * {@inheritDoc}
   */
  @Override
  ReflectionElement getGenericElement();

  // Functionality specific to the specialization
  /**
   * {@inheritDoc}
   */
  @Override
  java.lang.reflect.TypeVariable<?> getSource();
}