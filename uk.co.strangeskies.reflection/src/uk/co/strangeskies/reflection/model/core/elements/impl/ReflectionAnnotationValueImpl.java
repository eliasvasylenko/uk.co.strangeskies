package uk.co.strangeskies.reflection.model.core.elements.impl;

import java.util.Objects;

import javax.lang.model.element.AnnotationValueVisitor;

class ReflectionAnnotationValueImpl
    implements javax.lang.model.element.AnnotationValue {
  private Object value = null;

  protected ReflectionAnnotationValueImpl(Object value) {
    // Is this constraint really necessary?
    Objects.requireNonNull(value);
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
    return v.visit(this, p);
  }
}