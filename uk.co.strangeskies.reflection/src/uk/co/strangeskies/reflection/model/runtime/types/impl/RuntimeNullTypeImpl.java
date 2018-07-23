package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static javax.lang.model.type.TypeKind.NULL;
import static uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeNoTypeImpl.getNoneInstance;

import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNullType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;

public class RuntimeNullTypeImpl extends RuntimeTypeMirrorImpl implements RuntimeNullType {
  private static final RuntimeNullTypeImpl INSTANCE = new RuntimeNullTypeImpl();

  public static RuntimeNullType getInstance() {
    return INSTANCE;
  }

  private RuntimeNullTypeImpl() {}

  @Override
  public TypeKind getKind() {
    return NULL;
  }

  @Override
  public List<RuntimeTypeMirror> directSuperTypes() {
    return List.of(getNoneInstance());
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitNull(this, p);
  }
}