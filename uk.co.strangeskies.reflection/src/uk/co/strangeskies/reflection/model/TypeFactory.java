package uk.co.strangeskies.reflection.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

class TypeFactory {
  private TypeFactory() {}// no instances for you

  public static TypeMirror instance(Class<?> c) {
    if (c.isPrimitive()) {
      if (c.getName().equals("void")) {
        return ReflectionNoType.getVoidInstance();
      } else {
        return PrimitiveType.instance(c);
      }
    } else if (c.isArray()) {
      return new ReflectionArrayTypeImpl(c);
    } else if (c.isAnonymousClass() || c.isLocalClass() || c.isMemberClass() || c.isInterface() || // covers
                                                                                                   // annotations
        c.isEnum()) {
      return ReflectionDeclaredType.instance(c);
    } else { // plain old class ??
      return ReflectionDeclaredType.instance(c);
    }
  }

  public static TypeMirror instance(Type t) {
    if (t instanceof Class<?>) {
      return instance((Class<?>) t);
    } else if (t instanceof ParameterizedType) {
      ParameterizedType tmp = (ParameterizedType) t;
      Type raw = tmp.getRawType();
      if (!(raw instanceof Class)) {
        throw new IllegalArgumentException(t + " " + raw);
      }
      return ReflectionDeclaredType.instance((Class<?>) raw, tmp);
    } else if (t instanceof java.lang.reflect.WildcardType) {
      return new ReflectionWildcardType((java.lang.reflect.WildcardType) t);
    } else if (t instanceof java.lang.reflect.TypeVariable) {
      return new ReflectionTypeVariable((java.lang.reflect.TypeVariable<?>) t);
    }
    throw new IllegalArgumentException("Don't know how to make instance from: " + t.getClass());
  }

  public static TypeMirror instance(Field f) {
    return ReflectionDeclaredType.instance(f.getType(), f.getGenericType());
  }

  public static ExecutableType instance(Method m) {
    return new ExecutableMethodType(m);
  }

  public static javax.lang.model.type.TypeVariable typeVariableInstance(
      java.lang.reflect.TypeVariable<?> v) {
    return new ReflectionTypeVariable(v);
  }

  public static javax.lang.model.type.TypeVariable typeVariableInstance(
      TypeMirror source,
      TypeMirror upperBound,
      TypeMirror lowerBound) {
    return new CaptureTypeVariable(source, upperBound, lowerBound);
  }
}