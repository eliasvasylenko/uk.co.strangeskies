package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNoType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;

public class RuntimeNoTypeImpl extends RuntimeTypeMirrorImpl implements RuntimeNoType {
  private static final RuntimeNoTypeImpl NONE = new RuntimeNoTypeImpl(TypeKind.NONE);
  private static final RuntimeNoTypeImpl VOID = new RuntimeNoTypeImpl(TypeKind.VOID);
  private static final RuntimeNoTypeImpl PACKAGE = new RuntimeNoTypeImpl(TypeKind.PACKAGE);
  private static final RuntimeNoTypeImpl MODULE = new RuntimeNoTypeImpl(TypeKind.MODULE);

  private final TypeKind kind;

  public static RuntimeNoTypeImpl getNoneInstance() {
    return NONE;
  }

  public static RuntimeNoTypeImpl getVoidInstance() {
    return VOID;
  }

  public static RuntimeNoTypeImpl getPackageInstance() {
    return PACKAGE;
  }

  public static RuntimeNoTypeImpl getModuleInstance() {
    return MODULE;
  }

  private RuntimeNoTypeImpl(TypeKind kind) {
    this.kind = kind;
  }

  @Override
  public List<RuntimeTypeMirror> directSuperTypes() {
    return List.of(NONE);
  }

  @Override
  public String toString() {
    return kind.toString();
  }

  @Override
  public TypeKind getKind() {
    return kind;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitNoType(this, p);
  }
}
