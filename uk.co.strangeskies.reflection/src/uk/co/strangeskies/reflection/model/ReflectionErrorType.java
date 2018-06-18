package uk.co.strangeskies.reflection.model;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class ReflectionErrorType extends ReflectionTypeMirror
    implements javax.lang.model.type.ErrorType {
  private static ReflectionErrorType errorType = new ReflectionErrorType();

  public static ReflectionErrorType getErrorInstance() {
    return errorType;
  }

  private ReflectionErrorType() {
    super(TypeKind.ERROR);
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror getEnclosingType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Element asElement() {
    throw new UnsupportedOperationException();
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    throw new UnsupportedOperationException();
  }
}