package uk.co.strangeskies.reflection.model.core.elements;

import java.lang.reflect.Executable;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionParameterizable;

/**
 * A specialization of {@code javax.lang.model.element.ExecutableElement} that
 * is backed by core reflection.
 */
public interface ReflectionExecutableElement
    extends ReflectionElement, ExecutableElement, ReflectionParameterizable {

  /**
   * {@inheritDoc}
   */
  @Override
  List<ReflectionTypeParameterElement> getTypeParameters();

  /**
   * {@inheritDoc}
   */
  @Override
  List<ReflectionVariableElement> getParameters();

  // Functionality specific to the specialization
  /**
   * Returns all parameters, including synthetic ones.
   * 
   * @return all parameters, including synthetic ones
   */
  List<ReflectionVariableElement> getAllParameters();

  /**
   * {@inheritDoc}
   */
  @Override
  Executable getSource();

  /**
   * Returns true if this executable is a synthetic construct; returns false
   * otherwise.
   * 
   * @return true if this executable is a synthetic construct; returns false
   *         otherwise
   */
  boolean isSynthetic();

  /**
   * Returns true if this executable is a bridge method; returns false otherwise.
   * 
   * @return true if this executable is a bridge method; returns false otherwise
   */
  boolean isBridge();
}