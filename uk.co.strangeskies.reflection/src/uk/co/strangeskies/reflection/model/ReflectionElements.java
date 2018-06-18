package uk.co.strangeskies.reflection.model;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public interface ReflectionElements extends Elements {
  /**
   * Returns the innermost enclosing {@link ReflectionTypeElement} of the
   * {@link ReflectionElement} or {@code null} if the supplied ReflectionElement
   * is toplevel or represents a Package.
   *
   * @param e
   *          the {@link ReflectionElement} whose innermost enclosing
   *          {@link ReflectionTypeElement} is sought
   * @return the innermost enclosing {@link ReflectionTypeElement} or @{code null}
   *         if the parameter {@code e} is a toplevel element or a package
   */
  ReflectionTypeElement getEnclosingTypeElement(ReflectionElement e);

  /**
   * {@inheritDoc}
   */
  @Override
  List<? extends ReflectionElement> getAllMembers(TypeElement type);

  /**
   * {@inheritDoc}
   */
  @Override
  ReflectionPackageElement getPackageElement(CharSequence name);

  /**
   * {@inheritDoc}
   */
  @Override
  ReflectionPackageElement getPackageOf(Element type);

  /**
   * {@inheritDoc}
   */
  @Override
  ReflectionTypeElement getTypeElement(CharSequence name);
}