package uk.co.strangeskies.reflection.model.runtime;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.ExtendedTypes;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeElement;
import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeArrayType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeDeclaredType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNoType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNullType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimePrimitiveType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirrorProxy;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeVariable;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeWildcardType;

public interface RuntimeTypes extends ExtendedTypes {
  ReifiableRuntimeType asMirror(Type type);

  ReifiableRuntimeType asMirror(Class<?> clazz);

  RuntimeArrayType asMirror(GenericArrayType genericArrayType);

  RuntimeDeclaredType asMirror(ParameterizedType parameterizedType);

  RuntimeTypeVariable asMirror(java.lang.reflect.TypeVariable<?> typeVariable);

  RuntimeWildcardType asMirror(java.lang.reflect.WildcardType wildcardType);

  ReifiableRuntimeType asMirror(AnnotatedType type);

  RuntimeArrayType asMirror(AnnotatedArrayType annotatedArrayType);

  RuntimeDeclaredType asMirror(AnnotatedParameterizedType annotatedParameterizedType);

  RuntimeTypeVariable asMirror(AnnotatedTypeVariable annotatedTypeVariable);

  RuntimeWildcardType asMirror(AnnotatedWildcardType annotatedWildcardType);

  @Override
  RuntimeTypeMirrorProxy getProxy();

  @Override
  RuntimeTypeMirror getIntersection(TypeMirror... bounds);

  @Override
  RuntimeTypeMirror getUnion(TypeMirror... alternatives);

  @Override
  RuntimeElement asElement(TypeMirror t);

  @Override
  boolean isSameType(TypeMirror t1, TypeMirror t2);

  @Override
  boolean isSubtype(TypeMirror t1, TypeMirror t2);

  @Override
  boolean isAssignable(TypeMirror t1, TypeMirror t2);

  @Override
  boolean contains(TypeMirror t1, TypeMirror t2);

  @Override
  boolean isSubsignature(ExecutableType m1, ExecutableType m2);

  @Override
  List<RuntimeTypeMirror> directSupertypes(TypeMirror t);

  @Override
  RuntimeTypeMirror erasure(TypeMirror t);

  @Override
  RuntimeTypeElement boxedClass(PrimitiveType p);

  @Override
  RuntimePrimitiveType unboxedType(TypeMirror t);

  @Override
  RuntimeTypeMirror capture(TypeMirror t);

  @Override
  RuntimePrimitiveType getPrimitiveType(TypeKind kind);

  @Override
  RuntimeNullType getNullType();

  @Override
  RuntimeNoType getNoType(TypeKind kind);

  @Override
  RuntimeArrayType getArrayType(TypeMirror componentType);

  @Override
  RuntimeWildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound);

  @Override
  RuntimeDeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs);

  @Override
  RuntimeDeclaredType getDeclaredType(
      DeclaredType containing,
      TypeElement typeElem,
      TypeMirror... typeArgs);

  @Override
  RuntimeTypeMirror asMemberOf(DeclaredType containing, Element element);
}
