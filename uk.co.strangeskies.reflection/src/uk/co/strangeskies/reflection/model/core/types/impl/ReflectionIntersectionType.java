package uk.co.strangeskies.reflection.model.core.types.impl;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;

public class ReflectionIntersectionType extends ReflectionTypeMirror implements IntersectionType {
  private final List<ReflectionTypeMirror> bounds;

  public ReflectionIntersectionType(ReflectionTypeMirror[] sources) {
    super(TypeKind.INTERSECTION);
    this.bounds = List.of(requireNonNull(sources));
  }

  public ReflectionIntersectionType(Type[] sources) {
    this(stream(sources).map(TypeFactory::instance).toArray(ReflectionTypeMirror[]::new));
  }

  @Override
  public List<? extends ReflectionTypeMirror> directSuperTypes() {
    return getBounds();
  }

  @Override
  public List<? extends ReflectionTypeMirror> getBounds() {
    return bounds;
  }
}