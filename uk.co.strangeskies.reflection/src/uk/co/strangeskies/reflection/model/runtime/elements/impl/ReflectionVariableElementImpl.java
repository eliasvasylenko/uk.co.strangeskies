package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ElementVisitor;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElementVisitor;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeVariableElement;

abstract class ReflectionVariableElementImpl extends ReflectionElementImpl
    implements RuntimeVariableElement {

  protected ReflectionVariableElementImpl() {}

  // Element visitor
  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  // ReflectElement visitor
  @Override
  public <R, P> R accept(RuntimeElementVisitor<R, P> v, P p) {
    return v.visitVariable(this, p);
  }

  @Override
  public List<RuntimeElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public RuntimeElement getEnclosingElement() {
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