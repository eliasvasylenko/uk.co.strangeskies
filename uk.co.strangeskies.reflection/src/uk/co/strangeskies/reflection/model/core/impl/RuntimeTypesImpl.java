package uk.co.strangeskies.reflection.model.core.impl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.reflection.model.ExtendedTypes;
import uk.co.strangeskies.reflection.model.TypeMirrorProxy;
import uk.co.strangeskies.reflection.model.core.RuntimeTypes;
import uk.co.strangeskies.reflection.model.core.types.impl.ExecutableMethodType;
import uk.co.strangeskies.reflection.model.core.types.impl.ParameterizedDeclaredType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionDeclaredType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionGenericArrayTypeMirror;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionIntersectionType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionNoType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionNullType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionPrimitiveType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionTypeMirror;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionTypeMirrorProxy;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionTypeVariable;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionUnionType;
import uk.co.strangeskies.reflection.model.core.types.impl.ReflectionWildcardType;

public class RuntimeTypesImpl implements RuntimeTypes {
  private static final ExtendedTypes INSTANCE = new RuntimeTypesImpl();

  public static ExtendedTypes instance() {
    return INSTANCE;
  }

  // Private to suppress instantiation
  private RuntimeTypesImpl() {}

  // Types methods
  @Override
  public Element asElement(TypeMirror t) {
    checkType(t);
    if (t instanceof javax.lang.model.type.TypeVariable) {
      ((javax.lang.model.type.TypeVariable) t).asElement();
    } else if (t instanceof DeclaredType) {
      return ((DeclaredType) t).asElement();
    }
    return null;
  }

  @Override
  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
    if (t1.getKind() != t2.getKind()) {
      return false;
    }

    if (t1.getKind() == TypeKind.WILDCARD || t2.getKind() == TypeKind.WILDCARD) {
      // Wildcards are not equal to any type
      return false;
    }

    if (t1 instanceof ReflectionDeclaredType && t2 instanceof ReflectionDeclaredType) {
      return ((ReflectionDeclaredType) t1).isSameType((ReflectionDeclaredType) t2);
    } else if (t1 instanceof ReflectionPrimitiveType && t2 instanceof ReflectionPrimitiveType) {
      return t1.getKind() == t2.getKind();
    } else if (t1 instanceof ReflectionNoType && t2 instanceof ReflectionNoType) {
      return true;
    } else if (t1 instanceof NullType && t2 instanceof NullType) {
      return true;
    } else if (t1 instanceof ArrayType && t2 instanceof ArrayType) {
      return isSameType(((ArrayType) t1).getComponentType(), ((ArrayType) t2).getComponentType());
    }

    return false;
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
  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    checkType(t);
    if (t instanceof ExecutableType || t.getKind() == TypeKind.PACKAGE) {
      throw new IllegalArgumentException("You can't ask for direct supertypes for type: " + t);
    }
    return ((ReflectionTypeMirror) t).directSuperTypes();
  }

  @Override
  public TypeMirror erasure(TypeMirror t) {
    checkType(t);
    return ((ReflectionTypeMirror) t).erasure();
  }

  @Override
  public TypeElement boxedClass(javax.lang.model.type.PrimitiveType p) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ReflectionPrimitiveType unboxedType(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror capture(TypeMirror t) {
    checkType(t);
    return ((ReflectionTypeMirror) t).capture();
  }

  @Override
  public ReflectionPrimitiveType getPrimitiveType(TypeKind kind) {
    return ReflectionPrimitiveType.instance(kind);
  }

  @Override
  public NullType getNullType() {
    return ReflectionNullType.getInstance();
  }

  @Override
  public javax.lang.model.type.NoType getNoType(TypeKind kind) {
    if (kind == TypeKind.NONE) {
      return ReflectionNoType.getNoneInstance();
    } else if (kind == TypeKind.VOID) {
      return ReflectionNoType.getVoidInstance();
    } else {
      throw new IllegalArgumentException("No NoType of kind: " + kind);
    }
  }

  @Override
  public ArrayType getArrayType(TypeMirror componentType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public javax.lang.model.type.WildcardType getWildcardType(
      TypeMirror extendsBound,
      TypeMirror superBound) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DeclaredType getDeclaredType(
      DeclaredType containing,
      TypeElement typeElem,
      TypeMirror... typeArgs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror asMemberOf(javax.lang.model.type.DeclaredType containing, Element element) {
    throw new UnsupportedOperationException();
  }

  private void checkType(TypeMirror t) {
    if (!(t instanceof ReflectionTypeMirror)) {
      throw new IllegalArgumentException(
          "This Types implementation can only operate on CoreReflectionFactory type classes");
    }
  }

  @Override
  public TypeMirror getIntersection(TypeMirror... lowerBounds) {
    return new ReflectionIntersectionType(checkTypes(lowerBounds));
  }

  @Override
  public TypeMirror getUnion(TypeMirror... alternatives) {
    return new ReflectionUnionType(checkTypes(alternatives));
  }

  @Override
  public TypeMirrorProxy getProxy() {
    return new ReflectionTypeMirrorProxy();
  }

  private ReflectionTypeMirror[] checkTypes(TypeMirror[] lowerBounds) {
    for (var lowerBound : lowerBounds) {
      if (!(lowerBound instanceof ReflectionTypeMirror)) {
        throw new IllegalArgumentException();
      }
    }
    return (ReflectionTypeMirror[]) lowerBounds;
  }

  @Override
  public TypeMirror asMirror(Type type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeMirror asMirror(Class<?> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeclaredType asMirror(ParameterizedType parameterizedType) {
    return new ParameterizedDeclaredType(parameterizedType);
  }

  @Override
  public ArrayType asMirror(GenericArrayType genericArrayType) {
    return new ReflectionGenericArrayTypeMirror(genericArrayType);
  }

  @Override
  public TypeVariable asMirror(java.lang.reflect.TypeVariable<?> typeVariable) {
    return new ReflectionTypeVariable(typeVariable);
  }

  @Override
  public WildcardType asMirror(java.lang.reflect.WildcardType wildcardType) {
    return new ReflectionWildcardType(wildcardType);
  }
}