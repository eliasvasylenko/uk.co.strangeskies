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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.reflection.TypeHierarchy;
import uk.co.strangeskies.utility.Isomorphism;

public class TypeBounds {
  private final Isomorphism isomorphism = new Isomorphism();
  private final ExtendedTypes types;

  public TypeBounds(ExtendedTypes types) {
    this.types = types;
  }

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

      return types.getIntersection(bestTypes);
    }
  }

  void minimiseCandidates(
      MultiMap<DeclaredType, DeclaredType, ? extends Set<DeclaredType>> erasedCandidates) {
    List<DeclaredType> minimalCandidates = new ArrayList<>(erasedCandidates.keySet());
    if (minimalCandidates.size() > 1)
      for (int i = 0; i < minimalCandidates.size(); i++)
        for (int j = i + 1; j < minimalCandidates.size(); j++) {
          if (types.isAssignable(minimalCandidates.get(j), minimalCandidates.get(i))) {
            minimalCandidates.remove(i);
            j = i;
          } else if (types.isAssignable(minimalCandidates.get(i), minimalCandidates.get(j))) {
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
    TypeMirrorProxy proxy = types.getProxy();
    return isomorphism
        .byEquality()
        .getPartialMapping(new LinkedHashSet<>(parameterizations), () -> proxy, p -> {
          DeclaredType instance = bestImpl(rawClass, new ArrayList<>(p));
          proxy.setInstance(instance);
          return instance;
        });
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
      best = types
          .getDeclaredType(
              bestEnclosing,
              (TypeElement) rawClass.asElement(),
              leastContainingParameterization.stream().toArray(TypeMirror[]::new));
    } else {
      best = types
          .getDeclaredType(
              (TypeElement) rawClass.asElement(),
              leastContainingParameterization.stream().toArray(TypeMirror[]::new));
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
          return types.getWildcardType(leastUpperBoundImpl(aggregation), null);
        } else {
          /*
           * lcta(? extends U, ? super V) = U if U = V, otherwise ?
           */
          return wildcardU.getExtendsBound().equals(wildcardV.getSuperBound())
              ? wildcardU.getExtendsBound()
              : types.getWildcardType(null, null);
        }
      } else {
        /*
         * lcta(? super U, ? super V) = ? super glb(U, V)
         */
        return types
            .getWildcardType(
                null,
                types
                    .getIntersection(
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
        return types.getWildcardType(leastUpperBoundImpl(bounds), null);
      } else {
        /*
         * lcta(U, ? super V) = ? super glb(U, V)
         */
        return types
            .getWildcardType(
                null,
                types.getIntersection(argumentU, ((WildcardType) argumentV).getSuperBound()));
      }
    } else {
      /*
       * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
       */
      return argumentU.equals(argumentV)
          ? argumentU
          : types.getWildcardType(leastUpperBoundImpl(asList(argumentU, argumentV)), null);
    }
  }

  Map<DeclaredType, DeclaredType> getErasedSupertypes(TypeMirror of) {
    Map<DeclaredType, DeclaredType> supertypes = new HashMap<>();

    new TypeHierarchy(types, of).resolveCompleteSupertypeHierarchy(null).forEach(type -> {
      supertypes.put((DeclaredType) types.erasure(type), type);
    });

    return supertypes;
  }
}
