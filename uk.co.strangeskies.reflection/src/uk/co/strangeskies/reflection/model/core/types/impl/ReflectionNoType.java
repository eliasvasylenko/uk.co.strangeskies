package uk.co.strangeskies.reflection.model.core.types.impl;

import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ReflectionNoType extends ReflectionTypeMirror implements javax.lang.model.type.NoType {
  private static ReflectionNoType noneType = new ReflectionNoType(TypeKind.NONE, "none");
  private static ReflectionNoType packageType = new ReflectionNoType(TypeKind.PACKAGE, "package");
  private static ReflectionNoType voidType = new ReflectionNoType(TypeKind.VOID, "void");

  private String str;

  public static ReflectionNoType getNoneInstance() {
    return noneType;
  }

  public static ReflectionNoType getPackageInstance() {
    return packageType;
  }

  public static ReflectionNoType getVoidInstance() {
    return voidType;
  }

  private ReflectionNoType(TypeKind k, String str) {
    super(k);
    this.str = str;
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    // TODO We don't need this for the Package instance, how about the others?
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return str;
  }
}
