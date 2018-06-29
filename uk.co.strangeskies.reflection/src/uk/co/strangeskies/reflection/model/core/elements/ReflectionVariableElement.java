package uk.co.strangeskies.reflection.model.core.elements;

import javax.lang.model.element.VariableElement;

/**
 * A specialization of {@code javax.lang.model.element.VariableElement} that is
 * backed by core reflection.
 */
public interface ReflectionVariableElement extends ReflectionElement, VariableElement {

  // Functionality specific to the specialization
  /**
   * Returns true if this variable is a synthetic construct; returns false
   * otherwise.
   * 
   * @return true if this variable is a synthetic construct; returns false
   *         otherwise
   */
  boolean isSynthetic();

  /**
   * Returns true if this variable is implicitly declared in source code; returns
   * false otherwise.
   * 
   * @return true if this variable is implicitly declared in source code; returns
   *         false otherwise
   */
  boolean isImplicit();

  // The VariableElement concept covers fields, variables, and
  // method and constructor parameters. Therefore, this
  // interface cannot define a more precise override of
  // getSource since those three concept have different core
  // reflection types with no supertype more precise than
  // AnnotatedElement.
}