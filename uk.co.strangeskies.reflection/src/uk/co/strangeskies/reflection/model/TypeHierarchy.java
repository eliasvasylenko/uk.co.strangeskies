/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.model;

import static java.util.stream.Stream.empty;
import static uk.co.strangeskies.collection.stream.StreamUtilities.flatMapRecursive;
import static uk.co.strangeskies.collection.stream.StreamUtilities.iterateOptional;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;

public class TypeHierarchy {
  private final Types types;
  private final TypeMirror lowerBound;
  private final Map<DeclaredType, DeclaredType> supertypes;

  /**
   * @param types
   *          the type system context
   * @param lowerBound
   *          the type providing a context within which to determine the arguments
   *          of the supertypes
   */
  public TypeHierarchy(Types types, TypeMirror lowerBound) {
    this.types = types;
    this.lowerBound = lowerBound;
    this.supertypes = new HashMap<>();
  }

  public Stream<DeclaredType> resolveSuperClasses() {
    return StreamUtilities
        .iterate(lowerBound, t -> getSuperClass(t))
        .filter(t -> t.getKind() == TypeKind.DECLARED)
        .map(DeclaredType.class::cast);
  }

  private TypeMirror getSuperClass(TypeMirror type) {
    List<? extends TypeMirror> types = this.types.directSupertypes(type);
    if (types.isEmpty()) {
      return null;
    }

    while (this.types.asElement(types.get(0)).getKind().isInterface()) {
      type = types.get(0);
      types = this.types.directSupertypes(type);
    }

    return types.get(0);
  }

  private void validateSuperclass(DeclaredType superclass) {
    if (getAllTypeArguments(superclass).count() > 0) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotResolveGenericSupertype(lowerBound, superclass));
    }
    if (!types.isAssignable(lowerBound, superclass)) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotResolveSupertype(lowerBound, superclass));
    }
  }

  /**
   * Determine the recursive sequence of direct supertypes of a given type which
   * lead to either the given superclass or a parameterization thereof.
   * 
   * @param superclass
   *          the class of the supertype parameterization we wish to determine
   * @return a stream returning the given type and then each direct supertype
   *         recursively until the given superclass, or a parameterization
   *         thereof, is reached
   */
  public Stream<DeclaredType> resolveCompleteSupertypeHierarchy(DeclaredType superclass) {
    Set<DeclaredType> encountered = new HashSet<>();

    return flatMapRecursive(lowerBound, t -> resolveImmediateSupertypes(encountered, t, superclass))
        .filter(t -> t.getKind() == TypeKind.DECLARED)
        .map(DeclaredType.class::cast);
  }

  /**
   * Determine the super type of a given type which is either equal to the given
   * superclass or a parameterization thereof.
   * 
   * @param superclass
   *          the class of the supertype parameterization we wish to determine
   * @return the supertype of the requested class
   */
  public DeclaredType resolveSupertype(DeclaredType superclass) {
    if (supertypes.containsKey(superclass)) {
      return supertypes.get(superclass);
    }

    validateSuperclass(superclass);

    if (ParameterizedTypes.getAllTypeParameters(superclass).count() == 0) {
      return superclass;
    }

    return (DeclaredType) iterateOptional(
        lowerBound,
        t -> resolveImmediateSupertypes(null, t, superclass).findFirst()).reduce((a, b) -> b).get();
  }

  private Stream<? extends TypeMirror> resolveImmediateSupertypes(
      Set<DeclaredType> encountered,
      TypeMirror type,
      DeclaredType superclass) {
    if (type.getKind() == TypeKind.DECLARED && encountered != null && !encountered.isEmpty()) {
      encountered.remove(types.erasure(type));
    }

    List<? extends TypeMirror> lesserSubtypes = types.directSupertypes(type);

    if (lesserSubtypes.isEmpty()) {
      return empty();
    }

    /*
     * If there is more than one supertype in evaluation
     */
    boolean removeEncounters = encountered != null
        && (!encountered.isEmpty() || lesserSubtypes.size() > 1);

    for (Iterator<? extends TypeMirror> lesserSubtypeIterator = lesserSubtypes
        .iterator(); lesserSubtypeIterator.hasNext();) {
      TypeMirror lesserSubtype = lesserSubtypeIterator.next();

      if (superclass != null && !types.isAssignable(lesserSubtype, superclass)) {
        lesserSubtypeIterator.remove();

      } else if (removeEncounters && lesserSubtype.getKind() == TypeKind.DECLARED) {
        DeclaredType erasure = (DeclaredType) types.erasure(lesserSubtype);

        if (encountered.stream().anyMatch(e -> types.isAssignable(erasure, e))) {
          lesserSubtypeIterator.remove();

        } else {
          encountered.add(erasure);
        }
      }
    }

    return lesserSubtypes.stream().peek(subtype -> {
      if (subtype.getKind() == TypeKind.DECLARED) {
        supertypes.put((DeclaredType) types.erasure(subtype), (DeclaredType) subtype);
      }
    });
  }

  @Override
  public String toString() {
    return lowerBound + " <: " + supertypes;
  }
}
