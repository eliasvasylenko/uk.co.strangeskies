package uk.co.strangeskies.reflection.model.runtime.elements;

import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeParameterizable;

/**
 * A specialization of {@link javax.lang.model.element.TypeElement} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeTypeElement extends RuntimeElement, TypeElement, RuntimeParameterizable {
  @Override
  Class<?> getSource();

  /**
   * @return the members of the class
   */
  List<RuntimeElement> getAllMembers();

  /**
   * @return the binary name of the class
   */
  Name getBinaryName();

  @Override
  List<RuntimeAnnotationMirror> getAnnotationMirrors();
}
