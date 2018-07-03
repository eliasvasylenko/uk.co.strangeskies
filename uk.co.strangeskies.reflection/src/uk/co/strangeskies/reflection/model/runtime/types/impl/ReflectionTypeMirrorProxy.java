package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import uk.co.strangeskies.reflection.model.TypeMirrorProxy;

public class ReflectionTypeMirrorProxy implements TypeMirrorProxy {

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
  public List<? extends TypeVariable> getTypeVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getReturnType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends TypeMirror> getParameterTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getReceiverType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends TypeMirror> getAlternatives() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getComponentType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Element asElement() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getEnclosingType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getUpperBound() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getLowerBound() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getExtendsBound() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror getSuperBound() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setInstance(TypeMirror type) {
    // TODO Auto-generated method stub

  }

}
