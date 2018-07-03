package uk.co.strangeskies.reflection.model.runtime.impl;

import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.EXECUTABLE;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.type.TypeKind.PACKAGE;
import static javax.lang.model.type.TypeKind.TYPEVAR;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.RuntimeModel;
import uk.co.strangeskies.reflection.model.runtime.RuntimeTypes;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeElement;
import uk.co.strangeskies.reflection.model.runtime.types.ReifiableRuntimeType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeArrayType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeDeclaredType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeExecutableType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNoType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeNullType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimePrimitiveType;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirror;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeMirrorProxy;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeTypeVariable;
import uk.co.strangeskies.reflection.model.runtime.types.RuntimeWildcardType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ExecutableMethodType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ParameterizedTypeMirror;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionGenericArrayTypeMirror;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionIntersectionType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeNoTypeImpl;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionPrimitiveType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionTypeMirrorProxy;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionTypeVariable;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionUnionType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.ReflectionWildcardType;
import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeNullTypeImpl;
import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeTypeMirrorImpl;

public class RuntimeTypesImpl implements RuntimeTypes {
  private final RuntimeModel model;

  public RuntimeTypesImpl(RuntimeModel model) {
    this.model = model;
  }

  @Override
  public RuntimeElement asElement(TypeMirror type) {
    checkType(type);

    if (type.getKind() == TYPEVAR) {
      return ((RuntimeTypeVariable) type).asElement();

    } else if (type.getKind() == DECLARED) {
      return ((RuntimeTypeVariable) type).asElement();

    } else {
      return null;
    }
  }

  @Override
  public boolean isSameType(TypeMirror first, TypeMirror second) {
    checkType(first);
    checkType(second);

    if (first.getKind() != second.getKind()) {
      return false;
    }

    TypeKind kind = first.getKind();

    if (kind.isPrimitive()) {
      return true;
    }

    switch (kind) {
    case DECLARED:
      return Objects
          .equals(
              ((RuntimeDeclaredType) first).getSource().getType(),
              ((RuntimeDeclaredType) second).getSource().getType());

    case ARRAY:
      return isSameType(
          ((ArrayType) first).getComponentType(),
          ((ArrayType) second).getComponentType());

    case TYPEVAR:

    case EXECUTABLE:
      return Objects
          .equals(
              ((RuntimeExecutableType) first).getErasedSource(),
              ((RuntimeExecutableType) second).getErasedSource())
          && isSameType(
              ((RuntimeExecutableType) first).getReceiverType(),
              ((RuntimeExecutableType) second).getReceiverType());

    case UNION:

    case INTERSECTION:

    case NONE:
    case NULL:
      return true;

    default:
      return false;
    }
  }

  @Override
  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    checkType(t1);
    checkType(t2);

    if (isSameType(t1, t2)) {
      return true;
    } else if (t1.getKind() == TypeKind.NULL) {
      return true;
    }

    // This depth first traversal should terminate due to the ban on circular
    // inheritance
    List<? extends TypeMirror> directSupertypes = directSupertypes(t1);
    if (directSupertypes.isEmpty()) {
      return false;
    }
    for (TypeMirror ti : directSupertypes) {
      if (isSameType(ti, t2) || isSubtype(ti, t2)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(TypeMirror t1, TypeMirror t2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    checkType(m1);
    checkType(m2);

    ExecutableMethodType m0 = (ExecutableMethodType) m1;

    return m0.sameSignature((ExecutableMethodType) m2)
        || m0.sameSignature((ExecutableMethodType) erasure(m2));
  }

  @Override
  public List<RuntimeTypeMirror> directSupertypes(TypeMirror t) {
    if (t.getKind() == EXECUTABLE || t.getKind() == PACKAGE) {
      return List.of(getNoType(NONE));
    }

    return checkType(t).directSuperTypes();
  }

  @Override
  public RuntimeTypeMirror erasure(TypeMirror t) {
    return checkType(t).erasure();
  }

  @Override
  public RuntimeTypeElement boxedClass(javax.lang.model.type.PrimitiveType p) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuntimePrimitiveType unboxedType(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuntimeTypeMirror capture(TypeMirror t) {
    return checkType(t).capture();
  }

  @Override
  public RuntimePrimitiveType getPrimitiveType(TypeKind kind) {
    return ReflectionPrimitiveType.instance(kind);
  }

  @Override
  public RuntimeNullType getNullType() {
    return RuntimeNullTypeImpl.getInstance();
  }

  @Override
  public RuntimeNoType getNoType(TypeKind kind) {
    if (kind == TypeKind.NONE) {
      return RuntimeNoTypeImpl.getNoneInstance();
    } else if (kind == TypeKind.VOID) {
      return RuntimeNoTypeImpl.getVoidInstance();
    } else {
      throw new IllegalArgumentException("No NoType of kind: " + kind);
    }
  }

  @Override
  public RuntimeArrayType getArrayType(TypeMirror componentType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuntimeWildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuntimeDeclaredType getDeclaredType(
      DeclaredType containing,
      TypeElement typeElem,
      TypeMirror... typeArgs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeDeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeTypeMirror asMemberOf(
      javax.lang.model.type.DeclaredType containing,
      Element element) {
    throw new UnsupportedOperationException();
  }

  private RuntimeTypeMirrorImpl checkType(TypeMirror type) {
    if (!(type instanceof RuntimeTypeMirrorImpl)) {
      throw new IllegalArgumentException("Expecting reflective implementation ");
    }
    return (RuntimeTypeMirrorImpl) type;
  }

  @Override
  public RuntimeTypeMirror getIntersection(TypeMirror... lowerBounds) {
    return new ReflectionIntersectionType(checkTypes(lowerBounds));
  }

  @Override
  public RuntimeTypeMirror getUnion(TypeMirror... alternatives) {
    return new ReflectionUnionType(checkTypes(alternatives));
  }

  @Override
  public RuntimeTypeMirrorProxy getProxy() {
    return new ReflectionTypeMirrorProxy();
  }

  private RuntimeTypeMirrorImpl[] checkTypes(TypeMirror[] lowerBounds) {
    for (var lowerBound : lowerBounds) {
      if (!(lowerBound instanceof RuntimeTypeMirrorImpl)) {
        throw new IllegalArgumentException();
      }
    }
    return (RuntimeTypeMirrorImpl[]) lowerBounds;
  }

  @Override
  public ReifiableRuntimeType asMirror(Type type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReifiableRuntimeType asMirror(Class<?> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeDeclaredType asMirror(ParameterizedType parameterizedType) {
    return new ParameterizedTypeMirror(parameterizedType);
  }

  @Override
  public RuntimeArrayType asMirror(GenericArrayType genericArrayType) {
    return new ReflectionGenericArrayTypeMirror(genericArrayType);
  }

  @Override
  public RuntimeTypeVariable asMirror(java.lang.reflect.TypeVariable<?> typeVariable) {
    return new ReflectionTypeVariable(typeVariable);
  }

  @Override
  public RuntimeWildcardType asMirror(java.lang.reflect.WildcardType wildcardType) {
    return new ReflectionWildcardType(wildcardType);
  }

  @Override
  public ReifiableRuntimeType asMirror(AnnotatedType type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeArrayType asMirror(AnnotatedArrayType annotatedArrayType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeDeclaredType asMirror(AnnotatedParameterizedType annotatedParameterizedType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeTypeVariable asMirror(AnnotatedTypeVariable annotatedTypeVariable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeWildcardType asMirror(AnnotatedWildcardType annotatedWildcardType) {
    // TODO Auto-generated method stub
    return null;
  }
}