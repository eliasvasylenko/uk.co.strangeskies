package uk.co.strangeskies.reflection.model;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public interface TypeMirrorProxy
    extends TypeMirror, ExecutableType, UnionType, IntersectionType, PrimitiveType, NoType,
    ReferenceType, ArrayType, DeclaredType, ErrorType, NullType, TypeVariable, WildcardType {
  void setInstance(TypeMirror type);
}
