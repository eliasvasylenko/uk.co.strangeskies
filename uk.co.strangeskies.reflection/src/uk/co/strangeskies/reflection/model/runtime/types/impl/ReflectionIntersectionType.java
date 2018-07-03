package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;

public class ReflectionIntersectionType extends RuntimeTypeMirrorImpl implements IntersectionType {
  private final List<RuntimeTypeMirrorImpl> bounds;

  public ReflectionIntersectionType(RuntimeTypeMirrorImpl[] sources) {
    super(TypeKind.INTERSECTION);
    this.bounds = List.of(requireNonNull(sources));
  }

  public ReflectionIntersectionType(Type[] sources) {
    this(stream(sources).map(TypeFactory::instance).toArray(RuntimeTypeMirrorImpl[]::new));
  }

  @Override
  public List<? extends RuntimeTypeMirrorImpl> directSuperTypes() {
    return getBounds();
  }

  @Override
  public List<? extends RuntimeTypeMirrorImpl> getBounds() {
    return bounds;
  }
}