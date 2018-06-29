package uk.co.strangeskies.reflection.model.core.types.impl;

import javax.lang.model.type.TypeMirror;

public interface ReifiableReflectionType extends TypeMirror {
  Class<?> getSource();
}