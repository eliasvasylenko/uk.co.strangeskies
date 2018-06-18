package uk.co.strangeskies.reflection.model;

import java.util.List;

import javax.lang.model.element.Parameterizable;

/**
 * A specialization of {@code javax.lang.model.element.Parameterizable} being
 * backed by core reflection.
 */
public interface ReflectionParameterizable extends ReflectionElement, Parameterizable {
  @Override
  List<ReflectionTypeParameterElement> getTypeParameters();
}