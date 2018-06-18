package uk.co.strangeskies.reflection.model;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ElementVisitor;

abstract class ReflectionVariableElementImpl extends ReflectionElementImpl
    implements ReflectionVariableElement {

  protected ReflectionVariableElementImpl() {}

  // Element visitor
  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  // ReflectElement visitor
  @Override
  public <R, P> R accept(ReflectionElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public List<ReflectionElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public ReflectionElement getEnclosingElement() {
    return null;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isImplicit() {
    return false;
  }
}