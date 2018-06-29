package uk.co.strangeskies.reflection.model.core.types.impl;

import java.util.List;

import javax.lang.model.element.Parameterizable;

import uk.co.strangeskies.reflection.model.core.elements.ReflectionElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionTypeParameterElement;

/**
 * A specialization of {@code javax.lang.model.element.Parameterizable} being
 * backed by core reflection.
 */
public interface ReflectionParameterizable extends ReflectionElement, Parameterizable {
  @Override
  List<ReflectionTypeParameterElement> getTypeParameters();
}