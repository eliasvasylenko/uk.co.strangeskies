package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimePackageElement;
import uk.co.strangeskies.reflection.model.runtime.impl.ModifierUtil;

abstract class ReflectionElementImpl
    implements RuntimeElement, AnnotatedElement {
  @Override
  public abstract AnnotatedElement getSource();

  protected ReflectionElementImpl() {
    super();
  }

  // ReflectionElement methods
  @Override
  public RuntimePackageElement getPackage() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror asType() {
    throw new UnsupportedOperationException(getClass().toString());
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    Annotation[] annotations = getSource().getDeclaredAnnotations();
    int len = annotations.length;

    if (len > 0) {
      List<AnnotationMirror> res = new ArrayList<>(len);
      for (Annotation a : annotations) {
        res.add(CoreReflectionFactory.asMirror(a));
      }
      return Collections.unmodifiableList(res);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil.instance(0, false);
  }

  @Override
  public abstract Name getSimpleName();

  @Override
  public abstract RuntimeElement getEnclosingElement();

  @Override
  public abstract List<RuntimeElement> getEnclosedElements();

  // AnnotatedElement methods
  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return getSource().getAnnotation(annotationClass);
  }

  @Override
  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    return getSource().getAnnotationsByType(annotationClass);
  }

  @Override
  public Annotation[] getAnnotations() {
    return getSource().getAnnotations();
  }

  @Override
  public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
    return getSource().getDeclaredAnnotation(annotationClass);
  }

  @Override
  public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    return getSource().getDeclaredAnnotationsByType(annotationClass);
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return getSource().getDeclaredAnnotations();
  }

  // java.lang.Object methods
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ReflectionElementImpl) {
      ReflectionElementImpl other = (ReflectionElementImpl) obj;
      return Objects.equals(other.getSource(), this.getSource());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getSource());
  }

  @Override
  public String toString() {
    return getKind().toString() + " " + getSimpleName().toString();
  }
}