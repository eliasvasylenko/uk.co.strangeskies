package uk.co.strangeskies.reflection.model.core.types.impl;

import java.util.List;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ReflectionNullType extends ReflectionTypeMirror
    implements javax.lang.model.type.NullType {
  private static final ReflectionNullType NULL_TYPE = new ReflectionNullType();

  public static NullType getInstance() {
    return NULL_TYPE;
  }

  private ReflectionNullType() {
    super(TypeKind.NULL);
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    return List.of(ReflectionNoType.getNoneInstance());
  }
}