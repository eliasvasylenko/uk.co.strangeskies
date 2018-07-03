package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.model.runtime.RuntimeModel;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeAnnotationMirror;
import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;

public abstract class ReifiableRuntimeTypeImpl extends RuntimeTypeMirrorImpl
    implements ReifiableRuntimeType {
  private final RuntimeModel model;
  private List<RuntimeAnnotationMirror> annotations;

  public ReifiableRuntimeTypeImpl(RuntimeModel model) {
    this.model = model;
  }

  public RuntimeModel getModel() {
    return model;
  }

  @Override
  public List<RuntimeAnnotationMirror> getAnnotationMirrors() {
    if (annotations == null) {
      annotations = Stream
          .of(getSource().getDeclaredAnnotations())
          .map(model.elements()::asMirror)
          .collect(toList());
    }
    return annotations;
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return getSource().getAnnotation(annotationClass);
  }

  @Override
  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    return getSource().getAnnotationsByType(annotationClass);
  }
}