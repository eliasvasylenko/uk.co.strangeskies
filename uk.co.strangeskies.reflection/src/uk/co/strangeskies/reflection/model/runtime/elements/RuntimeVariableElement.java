package uk.co.strangeskies.reflection.model.runtime.elements;

import javax.lang.model.element.VariableElement;

/**
 * A specialization of {@link javax.lang.model.element.VariableElement} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeVariableElement extends RuntimeElement, VariableElement {
  /**
   * @return true if this variable is synthetic
   */
  boolean isSynthetic();

  /**
   * @return true if this variable is implicit
   */
  boolean isImplicit();
}