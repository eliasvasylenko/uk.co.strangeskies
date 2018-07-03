package uk.co.strangeskies.reflection.model.runtime.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import javax.lang.model.element.Element;

import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;

/**
 * A specialization of {@link javax.lang.model.element.Element} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeElement extends Element, AnnotatedElement {
  @Override
  RuntimeTypeMirror asType();

  /**
   * @return the underlying core reflection object
   */
  AnnotatedElement getSource();

  @Override
  RuntimeElement getEnclosingElement();

  @Override
  List<RuntimeElement> getEnclosedElements();

  /**
   * Applies a visitor to this element.
   *
   * @param <R>
   *          the return type of the visitor's methods
   * @param <P>
   *          the type of the additional parameter to the visitor's methods
   * @param v
   *          the visitor operating on this element
   * @param p
   *          additional parameter to the visitor
   * @return a visitor-specified result
   */
  <R, P> R accept(RuntimeElementVisitor<R, P> v, P p);

  /**
   * @return the package containing the element
   */
  RuntimePackageElement getPackage();

  @Override
  <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType);
}