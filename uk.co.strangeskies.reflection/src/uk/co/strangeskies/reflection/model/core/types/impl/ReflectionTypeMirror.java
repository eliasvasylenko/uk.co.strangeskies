package uk.co.strangeskies.reflection.model.core.types.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public abstract class ReflectionTypeMirror implements TypeMirror {
  private final TypeKind kind;

  protected ReflectionTypeMirror(TypeKind kind) {
    this.kind = Objects.requireNonNull(kind);
  }

  @Override
  public TypeKind getKind() {
    return kind;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visit(this, p);
  }

  // Types methods
  public abstract List<? extends TypeMirror> directSuperTypes();

  public TypeMirror capture() {
    // Exercise for the reader: make this abstract and implement in subtypes
    throw new UnsupportedOperationException();
  }

  public TypeMirror erasure() {
    // Exercise for the reader: make this abstract and implement in subtypes
    throw new UnsupportedOperationException();
  }

  // Exercise for the reader: implement the AnnotatedConstruct methods
  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    throw new UnsupportedOperationException();
  }
}