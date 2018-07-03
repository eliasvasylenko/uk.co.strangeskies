package uk.co.strangeskies.reflection.model.runtime.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

/**
 * As {@link javax.lang.model.element.ElementVisitor} for {@link RuntimeElement
 * runtime elements}.
 *
 * @param <R>
 *          the return type of this visitor's methods.
 * @param <P>
 *          the type of the additional parameter to this visitor's methods.
 */
@SuppressWarnings("javadoc")
public interface RuntimeElementVisitor<R, P> {
  /**
   * As {@link ElementVisitor#visit(Element, Object)}.
   */
  R visit(RuntimeElement e, P p);

  /**
   * As {@link ElementVisitor#visit(Element)}.
   */
  default R visit(RuntimeElement e) {
    return visit(e, null);
  }

  /**
   * As {@link ElementVisitor#visitModule(ModuleElement, Object)}.
   */
  R visitModule(RuntimeModuleElement e, P p);

  /**
   * As {@link ElementVisitor#visitPackage(PackageElement, Object)}.
   */
  R visitPackage(RuntimePackageElement e, P p);

  /**
   * As {@link ElementVisitor#visitType(TypeElement, Object)}.
   */
  R visitType(RuntimeTypeElement e, P p);

  /**
   * As {@link ElementVisitor#visitVariable(VariableElement, Object)}.
   */
  R visitVariable(RuntimeVariableElement e, P p);

  /**
   * As {@link ElementVisitor#visitExecutable(ExecutableElement, Object)}.
   */
  R visitExecutable(RuntimeExecutableElement e, P p);

  /**
   * As {@link ElementVisitor#visitTypeParameter(TypeParameterElement, Object)}.
   */
  R visitTypeParameter(RuntimeTypeParameterElement e, P p);

  /**
   * As {@link ElementVisitor#visitUnknown(Element, Object)}.
   */
  R visitUnknown(RuntimeElement e, P p);
}