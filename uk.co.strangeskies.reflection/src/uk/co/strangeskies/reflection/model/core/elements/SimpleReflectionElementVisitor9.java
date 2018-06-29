package uk.co.strangeskies.reflection.model.core.elements;

import static javax.lang.model.SourceVersion.RELEASE_9;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor9;

/**
 * Base class for simple visitors of elements that are backed by core
 * reflection.
 * 
 * @param <R>
 *          visitor method return type
 * @param <P>
 *          visitor method parameter type
 */
@SupportedSourceVersion(value = RELEASE_9)
public abstract class SimpleReflectionElementVisitor9<R, P> extends SimpleElementVisitor9<R, P>
    implements ReflectionElementVisitor<R, P> {

  protected SimpleReflectionElementVisitor9() {
    super();
  }

  protected SimpleReflectionElementVisitor9(R defaultValue) {
    super(defaultValue);
  }

  // Create manual "bridge methods" for now.

  @Override
  public final R visitPackage(PackageElement e, P p) {
    return visitPackage((ReflectionPackageElement) e, p);
  }

  @Override
  public final R visitType(TypeElement e, P p) {
    return visitType((ReflectionTypeElement) e, p);
  }

  @Override
  public final R visitVariable(VariableElement e, P p) {
    return visitVariable((ReflectionVariableElement) e, p);
  }

  @Override
  public final R visitExecutable(ExecutableElement e, P p) {
    return visitExecutable((ReflectionExecutableElement) e, p);
  }

  @Override
  public final R visitTypeParameter(TypeParameterElement e, P p) {
    return visitTypeParameter((ReflectionTypeParameterElement) e, p);
  }
}