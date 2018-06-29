package uk.co.strangeskies.reflection.model.core.elements;

import javax.lang.model.element.PackageElement;

public interface ReflectionPackageElement extends ReflectionElement, PackageElement {
  @Override
  Package getSource();
}