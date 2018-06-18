package uk.co.strangeskies.reflection.model;

import javax.lang.model.util.AbstractElementVisitor9;

/**
 * Base class for concrete visitors of elements backed by core reflection.
 */
public abstract class AbstractReflectionElementVisitor9<R, P>
    extends AbstractElementVisitor9<R, P> implements ReflectionElementVisitor<R, P> {
  protected AbstractReflectionElementVisitor9() {
    super();
  }
}