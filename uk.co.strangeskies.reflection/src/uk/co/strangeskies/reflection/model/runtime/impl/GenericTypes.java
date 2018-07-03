package uk.co.strangeskies.reflection.model.runtime.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class GenericTypes {
  public static boolean isSameGenericType(Type t1, Type t2) {
    if (t1 instanceof Class) {
      return ((Class<?>) t1).equals(t2);
    } else if (t1 instanceof ParameterizedType) {
      return ((ParameterizedType) t1).equals(t2);
    }
    throw new UnsupportedOperationException();
  }

  public static Type getEnclosingType(Type t1) {
    if (t1 instanceof Class) {
      return ((Class<?>) t1).getEnclosingClass();
    } else if (t1 instanceof ParameterizedType) {
      return ((ParameterizedType) t1).getOwnerType();
    }
    throw new UnsupportedOperationException();
  }
}