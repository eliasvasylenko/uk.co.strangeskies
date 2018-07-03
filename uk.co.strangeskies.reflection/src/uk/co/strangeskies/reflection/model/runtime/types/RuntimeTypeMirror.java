package uk.co.strangeskies.reflection.model.runtime.types;

import java.util.List;

import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeAnnotationMirror;

/**
 * A specialization of {@link javax.lang.model.type.TypeMirror} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeTypeMirror extends TypeMirror {
  @Override
  List<RuntimeAnnotationMirror> getAnnotationMirrors();
}
