package uk.co.strangeskies.reflection.model.runtime.elements;

import javax.lang.model.element.ModuleElement;

/**
 * A specialization of {@link javax.lang.model.element.ModuleElement} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeModuleElement extends RuntimeElement, ModuleElement {
  @Override
  Module getSource();
}
