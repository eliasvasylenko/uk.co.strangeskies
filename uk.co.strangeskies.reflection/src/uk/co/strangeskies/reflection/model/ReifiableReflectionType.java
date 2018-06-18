package uk.co.strangeskies.reflection.model;

import javax.lang.model.type.TypeMirror;

public interface ReifiableReflectionType extends TypeMirror {
  Class<?> getSource();
}