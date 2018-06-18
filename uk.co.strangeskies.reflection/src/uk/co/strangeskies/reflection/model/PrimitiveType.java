package uk.co.strangeskies.reflection.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveType extends ReflectionTypeMirror
    implements javax.lang.model.type.PrimitiveType, ReifiableReflectionType {
  private Class<?> source;

  private static PrimitiveType booleanInstance = new PrimitiveType(
      TypeKind.BOOLEAN,
      boolean.class);
  private static PrimitiveType byteInstance = new PrimitiveType(TypeKind.BYTE, byte.class);
  private static PrimitiveType charInstance = new PrimitiveType(TypeKind.CHAR, char.class);
  private static PrimitiveType shortInstance = new PrimitiveType(TypeKind.SHORT, short.class);
  private static PrimitiveType intInstance = new PrimitiveType(TypeKind.INT, int.class);
  private static PrimitiveType longInstance = new PrimitiveType(TypeKind.LONG, long.class);
  private static PrimitiveType floatInstance = new PrimitiveType(TypeKind.FLOAT, float.class);
  private static PrimitiveType doubleInstance = new PrimitiveType(TypeKind.DOUBLE, double.class);

  private PrimitiveType(TypeKind kind, Class<?> source) {
    super(kind);
    this.source = source;
  }

  @Override
  public Class<?> getSource() {
    return source;
  }

  static PrimitiveType instance(Class<?> c) {
    switch (c.getName()) {
    case "boolean":
      return booleanInstance;
    case "byte":
      return byteInstance;
    case "char":
      return charInstance;
    case "short":
      return shortInstance;
    case "int":
      return intInstance;
    case "long":
      return longInstance;
    case "float":
      return floatInstance;
    case "double":
      return doubleInstance;
    default:
      throw new IllegalArgumentException();
    }
  }

  static PrimitiveType instance(TypeKind k) {
    switch (k) {
    case BOOLEAN:
      return booleanInstance;
    case BYTE:
      return byteInstance;
    case CHAR:
      return charInstance;
    case SHORT:
      return shortInstance;
    case INT:
      return intInstance;
    case LONG:
      return longInstance;
    case FLOAT:
      return floatInstance;
    case DOUBLE:
      return doubleInstance;
    default:
      throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    return source.getName();
  }

  // Types methods
  @Override
  List<? extends TypeMirror> directSuperTypes() {
    switch (getKind()) {
    case DOUBLE:
      return Collections.emptyList();
    case FLOAT:
      return Arrays.asList(doubleInstance);
    case LONG:
      return Arrays.asList(floatInstance);
    case INT:
      return Arrays.asList(longInstance);
    case CHAR:
      return Arrays.asList(intInstance);
    case SHORT:
      return Arrays.asList(intInstance);
    case BYTE:
      return Arrays.asList(shortInstance);
    default:
      return Collections.emptyList();
    }
  }
}