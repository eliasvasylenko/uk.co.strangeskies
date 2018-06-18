package uk.co.strangeskies.reflection.model;

import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class ReflectionTypeVariable extends ReflectionTypeMirror
    implements javax.lang.model.type.TypeVariable {
  private final java.lang.reflect.TypeVariable<?> source;

  protected ReflectionTypeVariable(java.lang.reflect.TypeVariable<?> source) {
    super(TypeKind.TYPEVAR);
    Objects.requireNonNull(source);
    this.source = source;
  }

  @Override
  public TypeMirror getUpperBound() {
    return new IntersectionReferenceTypeImpl(source.getBounds());
  }

  @Override
  public TypeMirror getLowerBound() {
    return ReflectionTypes.instance().getNullType();
  }

  @Override
  public Element asElement() {
    return CoreReflectionFactory.createMirror(source);
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    return ((ReflectionTypeMirror) getUpperBound()).directSuperTypes();
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