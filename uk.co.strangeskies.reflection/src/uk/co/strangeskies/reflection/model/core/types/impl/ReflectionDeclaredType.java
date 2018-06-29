package uk.co.strangeskies.reflection.model.core.types.impl;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.core.CoreReflectionFactory;

public abstract class ReflectionDeclaredType extends ReflectionTypeMirror
    implements javax.lang.model.type.DeclaredType {
  private Class<?> source = null;

  public ReflectionDeclaredType(Class<?> source) {
    super(TypeKind.DECLARED);
    this.source = source;
  }

  protected Class<?> getSource() {
    return source;
  }

  @Override
  public Element asElement() {
    return CoreReflectionFactory.asMirror(getSource());
  }

  public abstract boolean isSameType(DeclaredType other);

  @Override
  public TypeMirror capture() {
    return TypeVariableCapture.captureWildcardArguments(this);
  }
}