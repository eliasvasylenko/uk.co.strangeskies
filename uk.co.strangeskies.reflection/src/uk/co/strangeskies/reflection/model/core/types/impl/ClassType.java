package uk.co.strangeskies.reflection.model.core.types.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ClassType extends ReflectionDeclaredType implements ReifiableReflectionType {
  public ClassType(Class<?> source) {
    super(source);
  }

  @Override
  public Class<?> getSource() {
    return super.getSource();
  }

  @Override
  public TypeMirror getEnclosingType() {
    Class<?> enclosing = getSource().getEnclosingClass();
    if (null == enclosing) {
      return ReflectionNoType.getNoneInstance();
    } else {
      return TypeFactory.instance(enclosing);
    }
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return Collections.emptyList();
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    if (getSource().isEnum()) {
      return enumSuper();
    }

    if (getSource() == java.lang.Object.class) {
      return Collections.emptyList();
    }
    List<TypeMirror> res = new ArrayList<>();
    Type[] superInterfaces = getSource().getInterfaces();
    if (!getSource().isInterface()) {
      res.add(TypeFactory.instance(getSource().getSuperclass()));
    } else if (superInterfaces.length == 0) {
      // Interfaces that don't extend another interface
      // have java.lang.Object as a direct supertype.
      return Collections
          .unmodifiableList(Arrays.asList(TypeFactory.instance(java.lang.Object.class)));
    }

    for (Type t : superInterfaces) {
      res.add(TypeFactory.instance(t));
    }
    return Collections.unmodifiableList(res);
  }

  private List<? extends TypeMirror> enumSuper() {
    Class<?> rawSuper = getSource().getSuperclass();
    Type[] actualArgs = ((ParameterizedTypeImpl) getSource().getGenericSuperclass())
        .getActualTypeArguments();

    // Reconsider this : assume the problem is making
    // Enum<MyEnum> rather than just a raw enum.
    return Collections
        .unmodifiableList(
            Arrays
                .asList(
                    TypeFactory
                        .instance(
                            new ParameterizedTypeImpl(
                                rawSuper,
                                Arrays.copyOf(actualArgs, actualArgs.length),
                                null))));
  }

  @Override
  boolean isSameType(DeclaredType other) {
    if (other instanceof ClassType) {
      return Objects.equals(getSource(), ((ClassType) other).getSource());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return getSource().toString();
  }
}