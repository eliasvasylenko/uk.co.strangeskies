package uk.co.strangeskies.reflection.model.runtime.elements;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;

import uk.co.strangeskies.reflection.model.runtime.types.RuntimeDeclaredType;

public interface RuntimeAnnotationMirror extends AnnotationMirror {
  Annotation getSource();

  @Override
  RuntimeDeclaredType getAnnotationType();

  @Override
  Map<RuntimeExecutableElement, AnnotationValue> getElementValues();
}
