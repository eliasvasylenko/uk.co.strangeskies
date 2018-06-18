package uk.co.strangeskies.reflection.model;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class ReflectionWildcardType extends ReflectionTypeMirror
    implements javax.lang.model.type.WildcardType {
  private final java.lang.reflect.WildcardType genericSource;

  ReflectionWildcardType(java.lang.reflect.WildcardType genericSource) {
    super(TypeKind.WILDCARD);
    this.genericSource = Objects.requireNonNull(genericSource);
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    // TODO Add support for this operation
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror getExtendsBound() {
    Type[] t = genericSource.getUpperBounds();

    if (t.length == 1) {
      if (t[0].equals(Object.class) && getSuperBound() != null) { // can't have both lower and
                                                                  // upper explicit
        return null;
      }
      return TypeFactory.instance(t[0]);
    }
    throw new UnsupportedOperationException(); // TODO: intersection type?
  }

  @Override
  public TypeMirror getSuperBound() {
    Type[] t = genericSource.getLowerBounds();

    if (t.length == 0) { // bound is null
      return null;
    } else if (t.length == 1) {
      return TypeFactory.instance(t[0]);
    }
    throw new UnsupportedOperationException(); // TODO: intersection type?
  }

  @Override
  public String toString() {
    return getKind() + " " + genericSource.toString();
  }
}