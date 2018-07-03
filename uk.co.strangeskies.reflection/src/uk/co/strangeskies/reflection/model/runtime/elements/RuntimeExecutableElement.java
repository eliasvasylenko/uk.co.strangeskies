package uk.co.strangeskies.reflection.model.runtime.elements;

import java.lang.reflect.Executable;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeParameterizable;

/**
 * A specialization of {@link javax.lang.model.element.ExecutableElement} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeExecutableElement
    extends RuntimeElement, ExecutableElement, RuntimeParameterizable {
  @Override
  Executable getSource();

  @Override
  List<RuntimeVariableElement> getParameters();

  /**
   * @return all parameters, including synthetic
   */
  List<RuntimeVariableElement> getAllParameters();

  /**
   * @return true if this executable is synthetic
   */
  boolean isSynthetic();

  /**
   * @return true if this executable is a bridge method
   */
  boolean isBridge();
}