package uk.co.strangeskies.reflection.model.core.elements;

import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionParameterizable;

/**
 * A specialization of {@code javax.lang.model.element.TypeElement} that is
 * backed by core reflection.
 */
public interface ReflectionTypeElement
    extends ReflectionElement, TypeElement, ReflectionParameterizable {

  /**
   * {@inheritDoc}
   */
  @Override
  List<ReflectionTypeParameterElement> getTypeParameters();

  /**
   * {@inheritDoc}
   */
  @Override
  List<ReflectionElement> getEnclosedElements();

  // Methods specific to the specialization, but functionality
  // also present in javax.lang.model.util.Elements.
  /**
   * Returns all members of a type element, whether inherited or declared
   * directly. For a class the result also includes its constructors, but not
   * local or anonymous classes.
   * 
   * @return all members of the type
   */
  List<ReflectionElement> getAllMembers();

  /**
   * Returns the binary name of a type element.
   * 
   * @return the binary name of a type element
   */
  Name getBinaryName();

  // Functionality specific to the specialization
  /**
   * {@inheritDoc}
   */
  @Override
  Class<?> getSource();
}