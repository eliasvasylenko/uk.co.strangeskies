package uk.co.strangeskies.reflection.model.runtime.types.impl;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;

import uk.co.strangeskies.reflection.model.TypeBounds;
import uk.co.strangeskies.reflection.model.runtime.impl.RuntimeTypesImpl;

public class ReflectionUnionType extends RuntimeTypeMirrorImpl implements UnionType {
  private final List<RuntimeTypeMirrorImpl> alternatives;
  private RuntimeTypeMirrorImpl lub;

  public ReflectionUnionType(RuntimeTypeMirrorImpl[] alternatives) {
    super(TypeKind.UNION);
    this.alternatives = List.of(requireNonNull(alternatives));
  }

  public ReflectionUnionType(Type[] sources) {
    this(stream(sources).map(TypeFactory::instance).toArray(RuntimeTypeMirrorImpl[]::new));
  }

  @Override
  public List<? extends RuntimeTypeMirrorImpl> directSuperTypes() {
    if (lub == null) {
      lub = (RuntimeTypeMirrorImpl) new TypeBounds(RuntimeTypesImpl.instance())
          .getLeastUpperBound(alternatives);
    }
    return asList(lub);
  }

  @Override
  public List<? extends TypeMirror> getAlternatives() {
    return alternatives;
  }
}
