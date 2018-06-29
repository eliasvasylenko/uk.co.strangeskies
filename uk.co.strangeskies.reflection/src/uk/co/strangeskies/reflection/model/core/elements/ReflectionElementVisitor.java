package uk.co.strangeskies.reflection.model.core.elements;

import javax.lang.model.element.UnknownElementException;

/**
 * A logical specialization of {@code
 * javax.lang.model.element.ElementVisitor} being backed by core reflection.
 *
 * @param <R>
 *          the return type of this visitor's methods.
 * @param <P>
 *          the type of the additional parameter to this visitor's methods.
 */
public interface ReflectionElementVisitor<R, P> {
  /**
   * Visits an element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visit(ReflectionElement e, P p);

  /**
   * A convenience method equivalent to {@code v.visit(e, null)}.
   * 
   * @param e
   *          the element to visit
   * @return a visitor-specified result
   */
  R visit(ReflectionElement e);

  /**
   * Visits a package element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitPackage(ReflectionPackageElement e, P p);

  /**
   * Visits a type element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitType(ReflectionTypeElement e, P p);

  /**
   * Visits a variable element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitVariable(ReflectionVariableElement e, P p);

  /**
   * Visits an executable element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitExecutable(ReflectionExecutableElement e, P p);

  /**
   * Visits a type parameter element.
   * 
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   */
  R visitTypeParameter(ReflectionTypeParameterElement e, P p);

  /**
   * Visits an unknown kind of element. This can occur if the language evolves and
   * new kinds of elements are added to the {@code Element} hierarchy.
   *
   * @param e
   *          the element to visit
   * @param p
   *          a visitor-specified parameter
   * @return a visitor-specified result
   * @throws UnknownElementException
   *           a visitor implementation may optionally throw this exception
   */
  R visitUnknown(ReflectionElement e, P p);
}