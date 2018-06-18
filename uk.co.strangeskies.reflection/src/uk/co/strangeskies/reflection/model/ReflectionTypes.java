package uk.co.strangeskies.reflection.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.reflection.TypeHierarchy;
import uk.co.strangeskies.utility.Isomorphism;

class ReflectionTypes implements ExtendedTypes {
  private static final ExtendedTypes INSTANCE = new ReflectionTypes();

  public static ExtendedTypes instance() {
    return INSTANCE;
  }

  private final Isomorphism isomorphism = new Isomorphism();

  // Private to suppress instantiation
  private ReflectionTypes() {}

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
    } else if (t1 instanceof PrimitiveType && t2 instanceof PrimitiveType) {
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
  public PrimitiveType unboxedType(TypeMirror t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeMirror capture(TypeMirror t) {
    checkType(t);
    return ((ReflectionTypeMirror) t).capture();
  }

  @Override
  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return PrimitiveType.instance(kind);
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
  public TypeMirror getLeastUpperBound(Collection<? extends TypeMirror> upperBounds) {
    TypeMirror upperBound = leastUpperBoundImpl(upperBounds);

    /*
     * Not sure if this is necessary! But it's cheap enough to check. Can't validate
     * IntersectionTypes and ParameterizedTypes as we create them, as they may
     * contain uninitialised proxies in place of ParameterizedTypes.
     */
    // validate(upperBound);

    return upperBound;
  }

  TypeMirror leastUpperBoundImpl(Collection<? extends TypeMirror> upperBounds) {
    if (upperBounds.size() == 1)
      /*
       * If k = 1, then the lub is the type itself: lub(U) = U.
       */
      return upperBounds.iterator().next();
    else {
      /*
       * For each Ui (1 ≤ i ≤ k):
       */
      Iterator<? extends TypeMirror> lowerBoundsIterator = upperBounds.iterator();
      MultiMap<DeclaredType, DeclaredType, ? extends Set<DeclaredType>> erasedCandidates = new MultiHashMap<>(
          HashSet::new);
      erasedCandidates.addAll(getErasedSupertypes(lowerBoundsIterator.next()));

      while (lowerBoundsIterator.hasNext()) {
        TypeMirror t = lowerBoundsIterator.next();
        Map<DeclaredType, DeclaredType> erasedSupertypes = getErasedSupertypes(t);
        erasedCandidates.keySet().retainAll(erasedSupertypes.keySet());
        for (Map.Entry<DeclaredType, DeclaredType> erasedSupertype : erasedSupertypes.entrySet())
          if (erasedCandidates.containsKey(erasedSupertype.getKey())
              && erasedSupertype.getValue() != null)
            erasedCandidates.add(erasedSupertype.getKey(), erasedSupertype.getValue());
      }

      minimiseCandidates(erasedCandidates);

      List<DeclaredType> bestTypes = erasedCandidates
          .entrySet()
          .stream()
          .map(e -> best(e.getKey(), new ArrayList<>(e.getValue())))
          .collect(Collectors.toList());

      return getIntersection(bestTypes);
    }
  }

  void minimiseCandidates(
      MultiMap<DeclaredType, DeclaredType, ? extends Set<DeclaredType>> erasedCandidates) {
    List<DeclaredType> minimalCandidates = new ArrayList<>(erasedCandidates.keySet());
    if (minimalCandidates.size() > 1)
      for (int i = 0; i < minimalCandidates.size(); i++)
        for (int j = i + 1; j < minimalCandidates.size(); j++) {
          if (isAssignable(minimalCandidates.get(j), minimalCandidates.get(i))) {
            minimalCandidates.remove(i);
            j = i;
          } else if (isAssignable(minimalCandidates.get(i), minimalCandidates.get(j))) {
            minimalCandidates.remove(j--);
          }
        }
    erasedCandidates.keySet().retainAll(minimalCandidates);
  }

  /**
   * Given a number of candidate parameterizations of a given class, derive the
   * most specific possible parameterization which is a supertype of all
   * candidates according to the Java 8 language specification regarding type
   * inference.uncheckedIntersectionOf
   * 
   * @param rawClass
   *          the class to be parameterized
   * @param parameterizations
   *          the candidate parameterizations
   * @return the parameterized type which minimally contains all the given types
   */
  public DeclaredType best(DeclaredType rawClass, List<DeclaredType> parameterizations) {
    if (parameterizations.isEmpty())
      return rawClass;
    else if (parameterizations.size() == 1) {
      DeclaredType parameterization = parameterizations.iterator().next();
      return parameterization == null ? rawClass : parameterization;
    }

    /*
     * Proxy guard against recursive generation of infinite types
     */
    return isomorphism
        .byEquality()
        .getProxiedMapping(
            new LinkedHashSet<>(parameterizations),
            DeclaredType.class,
            p -> bestImpl(rawClass, new ArrayList<>(p)));
  }

  DeclaredType bestImpl(DeclaredType rawClass, List<DeclaredType> parameterizations) {
    List<TypeMirror> leastContainingParameterization = new ArrayList<>(
        parameterizations.get(0).getTypeArguments());

    for (int i = 1; i < parameterizations.size(); i++) {
      DeclaredType parameterization = parameterizations.get(i);

      List<? extends TypeMirror> arguments = parameterization.getTypeArguments();

      for (int j = 0; j < arguments.size(); j++) {
        TypeMirror argumentV = leastContainingParameterization.get(j);
        TypeMirror argumentU = arguments.get(j);

        leastContainingParameterization.set(j, leastContainingArgument(argumentU, argumentV));
      }
    }

    DeclaredType best;

    if (rawClass.getEnclosingType().getKind() == TypeKind.DECLARED) {
      DeclaredType rawEnclosing = (DeclaredType) rawClass.getEnclosingType();
      List<DeclaredType> enclosingParameterizations = parameterizations
          .stream()
          .map(DeclaredType::getEnclosingType)
          .map(DeclaredType.class::cast)
          .collect(toList());
      DeclaredType bestEnclosing = bestImpl(rawEnclosing, enclosingParameterizations);
      best = getDeclaredType(
          bestEnclosing,
          (TypeElement) rawClass.asElement(),
          leastContainingParameterization);
    } else {
      best = getDeclaredType((TypeElement) rawClass.asElement(), leastContainingParameterization);
    }

    return best;
  }

  /**
   * Fetch the least containing argument of type type arguments according to the
   * Java 8 language specification.
   * 
   * @param argumentU
   *          the first argument
   * @param argumentV
   *          the second argument
   * @return the type argument which minimally contains both the given type
   *         arguments
   */
  public TypeMirror leastContainingArgument(TypeMirror argumentU, TypeMirror argumentV) {
    if (argumentU instanceof WildcardType && (!(argumentV instanceof WildcardType)
        || ((WildcardType) argumentV).getExtendsBound() != null)) {
      TypeMirror swap = argumentU;
      argumentU = argumentV;
      argumentV = swap;
    }

    if (argumentU instanceof WildcardType) {
      WildcardType wildcardU = (WildcardType) argumentU;
      WildcardType wildcardV = (WildcardType) argumentV;

      if (wildcardU.getExtendsBound() != null) {
        if (wildcardV.getExtendsBound() != null) {
          /*
           * lcta(? extends U, ? extends V) = ? extends lub(U, V)
           */
          List<TypeMirror> aggregation = Arrays
              .asList(wildcardU.getExtendsBound(), wildcardV.getExtendsBound());
          return getWildcardType(leastUpperBoundImpl(aggregation), null);
        } else {
          /*
           * lcta(? extends U, ? super V) = U if U = V, otherwise ?
           */
          return wildcardU.getExtendsBound().equals(wildcardV.getSuperBound())
              ? wildcardU.getExtendsBound()
              : getWildcardType(null, null);
        }
      } else {
        /*
         * lcta(? super U, ? super V) = ? super glb(U, V)
         */
        return getWildcardType(
            null,
            getIntersection(
                ((WildcardType) argumentU).getSuperBound(),
                ((WildcardType) argumentV).getSuperBound()));
      }
    } else if (argumentV instanceof WildcardType) {
      if (((WildcardType) argumentV).getExtendsBound() != null) {
        /*
         * lcta(U, ? extends V) = ? extends lub(U, V)
         */
        List<TypeMirror> bounds = new ArrayList<>(2);
        bounds.add(((WildcardType) argumentV).getExtendsBound());
        bounds.add(argumentU);
        return getWildcardType(leastUpperBoundImpl(bounds), null);
      } else {
        /*
         * lcta(U, ? super V) = ? super glb(U, V)
         */
        return getWildcardType(
            null,
            getIntersection(argumentU, ((WildcardType) argumentV).getSuperBound()));
      }
    } else {
      /*
       * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
       */
      return argumentU.equals(argumentV)
          ? argumentU
          : getWildcardType(leastUpperBoundImpl(asList(argumentU, argumentV)), null);
    }
  }

  Map<DeclaredType, DeclaredType> getErasedSupertypes(TypeMirror of) {
    Map<DeclaredType, DeclaredType> supertypes = new HashMap<>();

    new TypeHierarchy(this, of).resolveCompleteSupertypeHierarchy(null).forEach(type -> {
      supertypes.put((DeclaredType) erasure(type), type);
    });

    return supertypes;
  }

  @Override
  public TypeMirror getIntersection(Collection<? extends TypeMirror> lowerBounds) {
    return getIntersection(lowerBounds);
  }
}