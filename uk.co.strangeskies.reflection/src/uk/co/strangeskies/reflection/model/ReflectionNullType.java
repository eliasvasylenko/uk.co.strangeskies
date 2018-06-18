package uk.co.strangeskies.reflection.model;

import java.util.List;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class ReflectionNullType extends ReflectionTypeMirror
    implements javax.lang.model.type.NullType {
  private static ReflectionNullType nullType = new ReflectionNullType();

  public static NullType getInstance() {
    return nullType;
  }

  private ReflectionNullType() {
    super(TypeKind.NULL);
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    // JLS 4.10.2 says:
    // "The direct supertypes of the null type are all reference types other than
    // the null type itself."
    // TODO return null? an empty list? the error type? anyhow fix this
    throw new UnsupportedOperationException();
  }
}