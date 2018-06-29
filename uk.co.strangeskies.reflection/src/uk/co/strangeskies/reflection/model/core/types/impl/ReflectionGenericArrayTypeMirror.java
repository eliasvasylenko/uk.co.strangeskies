package uk.co.strangeskies.reflection.model.core.types.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public class ReflectionGenericArrayTypeMirror implements ArrayType {

  public ReflectionGenericArrayTypeMirror(GenericArrayType genericArrayType) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public TypeKind getKind() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getComponentType() {
    // TODO Auto-generated method stub
    return null;
  }

}
