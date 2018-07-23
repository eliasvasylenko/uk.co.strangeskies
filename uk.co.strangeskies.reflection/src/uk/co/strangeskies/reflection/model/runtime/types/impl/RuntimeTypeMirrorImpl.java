package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.emptyList;

import java.lang.annotation.Annotation;
import java.util.List;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeAnnotationMirror;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;

public abstract class RuntimeTypeMirrorImpl implements RuntimeTypeMirror {
  public abstract List<RuntimeTypeMirror> directSuperTypes();

  @Override
  public List<RuntimeAnnotationMirror> getAnnotationMirrors() {
    return emptyList();
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    return (T[]) newInstance(annotationClass, 0);
  }

  /*
   * For classes which may appear in an infinite type, to allow detection of
   * recursion such that a suitable finite string representation may be chosen.
   */
  protected abstract String toString(List<RuntimeTypeMirror> stack);
}