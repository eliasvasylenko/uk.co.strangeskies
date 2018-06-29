package uk.co.strangeskies.reflection.model.core.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * A specialization of {@code javax.lang.model.element.Element} that is backed
 * by core reflection.
 */
public interface ReflectionElement extends Element, AnnotatedElement {

  /**
   * {@inheritDoc}
   */
  @Override
  ReflectionElement getEnclosingElement();

  /**
   * {@inheritDoc}
   */
  @Override
  List<ReflectionElement> getEnclosedElements();

  /**
   * Applies a visitor to this element.
   *
   * @param v
   *          the visitor operating on this element
   * @param p
   *          additional parameter to the visitor
   * @param <R>
   *          the return type of the visitor's methods
   * @param <P>
   *          the type of the additional parameter to the visitor's methods
   * @return a visitor-specified result
   */
  <R, P> R accept(ReflectionElementVisitor<R, P> v, P p);

  // Functionality specific to the specialization
  /**
   * Returns the underlying core reflection source object, if applicable.
   * 
   * @return the underlying core reflection source object, if applicable
   */
  AnnotatedElement getSource();

  // Functionality from javax.lang.model.util.Elements
  /**
   * Returns the package of an element. The package of a package is itself.
   * 
   * @return the package of an element
   */
  ReflectionPackageElement getPackage();

  @Override
  <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType);
}