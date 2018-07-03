package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.impl.RuntimeTypesImpl;

public class ExecutableMethodType extends RuntimeTypeMirrorImpl
    implements javax.lang.model.type.ExecutableType {
  private final Method m;

  public ExecutableMethodType(Method m) {
    super(TypeKind.EXECUTABLE);
    this.m = Objects.requireNonNull(m);
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    Type[] exceptions = m.getGenericExceptionTypes();
    int len = exceptions.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<TypeMirror>(len);
      for (Type t : exceptions) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      List<TypeMirror> ret = Collections.emptyList();
      return ret;
    }
  }

  @Override
  public List<javax.lang.model.type.TypeVariable> getTypeVariables() {
    java.lang.reflect.TypeVariable[] variables = m.getTypeParameters();
    int len = variables.length;

    if (len > 0) {
      List<javax.lang.model.type.TypeVariable> res = new ArrayList<>(len);
      for (java.lang.reflect.TypeVariable<?> t : variables) {
        res.add(new ReflectionTypeVariable(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public TypeMirror getReturnType() {
    return TypeFactory.instance(m.getGenericReturnType());
  }

  @Override
  public List<? extends TypeMirror> getParameterTypes() {
    Type[] params = m.getGenericParameterTypes();
    int len = params.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<TypeMirror>(len);
      for (Type t : params) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    // Spec says we don't need this
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror erasure() {
    return new ErasedMethodType(m);
  }

  @Override
  public TypeMirror getReceiverType() {
    throw new UnsupportedOperationException();
  }

  public boolean sameSignature(ExecutableMethodType other) {
    if (!m.getName().equals(other.m.getName())) {
      return false;
    }

    List<? extends TypeMirror> thisParams = getParameterTypes();
    List<? extends TypeMirror> otherParams = other.getParameterTypes();
    if (thisParams.size() != otherParams.size()) {
      return false;
    }
    for (int i = 0; i < thisParams.size(); i++) {
      if (!RuntimeTypesImpl.instance().isSameType(thisParams.get(i), otherParams.get(i))) {
        return false;
      }
    }
    return true;
  }
}