package uk.co.strangeskies.reflection.model.runtime.types;

import java.util.List;

import javax.lang.model.type.IntersectionType;

/**
 * A specialization of {@link javax.lang.model.type.IntersectionType} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeIntersectionType extends RuntimeTypeMirror, IntersectionType {
  @Override
  List<RuntimeTypeMirror> getBounds();
}
