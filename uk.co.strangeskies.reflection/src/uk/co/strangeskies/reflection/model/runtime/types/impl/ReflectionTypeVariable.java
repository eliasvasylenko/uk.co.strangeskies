package uk.co.strangeskies.reflection.model.runtime.types.impl;

import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.impl.RuntimeTypesImpl;

public class ReflectionTypeVariable extends RuntimeTypeMirrorImpl implements TypeVariable {
  private final java.lang.reflect.TypeVariable<?> source;

  public ReflectionTypeVariable(java.lang.reflect.TypeVariable<?> source) {
    super(TypeKind.TYPEVAR);
    Objects.requireNonNull(source);
    this.source = source;
  }

  @Override
  public TypeMirror getUpperBound() {
    return new ReflectionIntersectionType(source.getBounds());
  }

  @Override
  public TypeMirror getLowerBound() {
    return RuntimeTypesImpl.instance().getNullType();
  }

  @Override
  public Element asElement() {
    return CoreReflectionFactory.asMirror(source);
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    return ((RuntimeTypeMirrorImpl) getUpperBound()).directSuperTypes();
  }

  @Override
  public int hashCode() {
    return source.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ReflectionTypeVariable) {
      return this.source.equals(((ReflectionTypeVariable) other).source);
    } else {
      return false;
    }
  }
}