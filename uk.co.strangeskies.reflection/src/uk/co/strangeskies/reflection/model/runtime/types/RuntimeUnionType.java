package uk.co.strangeskies.reflection.model.runtime.types;

import java.util.List;

import javax.lang.model.type.UnionType;

/**
 * A specialization of {@link javax.lang.model.type.UnionType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeUnionType extends RuntimeTypeMirror, UnionType {
  @Override
  List<RuntimeTypeMirror> getAlternatives();
}
