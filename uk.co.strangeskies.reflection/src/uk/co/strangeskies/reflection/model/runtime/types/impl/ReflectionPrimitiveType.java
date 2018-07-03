package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;

public class ReflectionPrimitiveType extends RuntimeTypeMirrorImpl
    implements PrimitiveType, ReifiableRuntimeType {
  private Class<?> source;

  private static ReflectionPrimitiveType booleanInstance = new ReflectionPrimitiveType(
      TypeKind.BOOLEAN,
      boolean.class);
  private static ReflectionPrimitiveType byteInstance = new ReflectionPrimitiveType(
      TypeKind.BYTE,
      byte.class);
  private static ReflectionPrimitiveType charInstance = new ReflectionPrimitiveType(
      TypeKind.CHAR,
      char.class);
  private static ReflectionPrimitiveType shortInstance = new ReflectionPrimitiveType(
      TypeKind.SHORT,
      short.class);
  private static ReflectionPrimitiveType intInstance = new ReflectionPrimitiveType(
      TypeKind.INT,
      int.class);
  private static ReflectionPrimitiveType longInstance = new ReflectionPrimitiveType(
      TypeKind.LONG,
      long.class);
  private static ReflectionPrimitiveType floatInstance = new ReflectionPrimitiveType(
      TypeKind.FLOAT,
      float.class);
  private static ReflectionPrimitiveType doubleInstance = new ReflectionPrimitiveType(
      TypeKind.DOUBLE,
      double.class);

  private ReflectionPrimitiveType(TypeKind kind, Class<?> source) {
    super(kind);
    this.source = source;
  }

  @Override
  public Class<?> getSource() {
    return source;
  }

  public static ReflectionPrimitiveType instance(Class<?> c) {
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

  public static ReflectionPrimitiveType instance(TypeKind k) {
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

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
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