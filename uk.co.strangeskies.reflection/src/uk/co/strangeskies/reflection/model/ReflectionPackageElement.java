package uk.co.strangeskies.reflection.model;

import javax.lang.model.element.PackageElement;

/**
 * A specialization of {@code javax.lang.model.element.PackageElement} being
 * backed by core reflection.
 */
public interface ReflectionPackageElement extends ReflectionElement, PackageElement {

  // Functionality specific to the specialization
  /**
   * {@inheritDoc}
   */
  @Override
  Package getSource();
}