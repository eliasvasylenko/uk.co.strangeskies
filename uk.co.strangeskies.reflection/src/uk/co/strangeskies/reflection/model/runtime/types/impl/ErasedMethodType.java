package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.type.TypeMirror;

class ErasedMethodType extends ExecutableMethodType
    implements javax.lang.model.type.ExecutableType {
  private final Method m;

  ErasedMethodType(Method m) {
    super(m);
    this.m = Objects.requireNonNull(m);
  }

  @Override
  public List<javax.lang.model.type.TypeVariable> getTypeVariables() {
    return Collections.emptyList();
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    Class<?>[] exceptions = m.getExceptionTypes();
    int len = exceptions.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<TypeMirror>(len);
      for (Class<?> t : exceptions) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      List<TypeMirror> ret = Collections.emptyList();
      return ret;
    }
  }

  @Override
  public List<? extends TypeMirror> getParameterTypes() {
    Class<?>[] params = m.getParameterTypes();
    int len = params.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<TypeMirror>(len);
      for (Class<?> t : params) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      List<TypeMirror> ret = Collections.emptyList();
      return ret;
    }
  }

  @Override
  public TypeMirror getReturnType() {
    return TypeFactory.instance(m.getReturnType());
  }

  @Override
  public TypeMirror erasure() {
    return this;
  }
}