package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.util.List;

import javax.lang.model.element.Parameterizable;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeParameterElement;

/**
 * A specialization of {@code javax.lang.model.element.Parameterizable} which
 * operates over the core reflection API.
 */
public interface RuntimeParameterizable extends RuntimeElement, Parameterizable {
  @Override
  List<RuntimeTypeParameterElement> getTypeParameters();
}