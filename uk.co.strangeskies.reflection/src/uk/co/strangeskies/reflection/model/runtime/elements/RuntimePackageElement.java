package uk.co.strangeskies.reflection.model.runtime.elements;

import javax.lang.model.element.PackageElement;

/**
 * A specialization of {@link javax.lang.model.element.PackageElement} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimePackageElement extends RuntimeElement, PackageElement {
  @Override
  Package getSource();
}