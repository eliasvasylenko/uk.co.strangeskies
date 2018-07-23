package uk.co.strangeskies.reflection.model;

import java.util.Map;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

public interface ExtendedTypes extends Types {
  /**
   * Returns a new intersection type of the given bounds.
   *
   * @param bounds
   *          the types to intersect
   * @return a new intersection
   * @throws IllegalArgumentException
   *           if bounds are not valid
   */
  TypeMirror getIntersection(TypeMirror... bounds);

  /**
   * Returns a new union type with of given alternatives.
   *
   * @param alternatives
   *          the types to unite
   * @return a new union
   * @throws IllegalArgumentException
   *           if alternatives are not valid
   */
  TypeMirror getUnion(TypeMirror... alternatives);

  default boolean isErased(TypeMirror type) {
    return isSameType(type, erasure(type));
  }

  default boolean isRaw(TypeMirror type) {
    return isSameType(type, erasure(type));
  }

  TypeVariable getTypeVariable(String name);

  /**
   * Substitute occurrences of type variables within a given type with appropriate
   * instantiations.
   * <p>
   * If a type variable is instantiated with a type which contains one of the type
   * variables to be instantiated, then substitution is applied recursively. It is
   * therefore possible to construct an infinite type.
   * 
   * @param type
   *          a type which may contain occurrences of type variables which require
   *          instantiation
   * @param instantiations
   *          a mapping from type variables which may occur in the given type to
   *          their instantiations in the resulting type
   * @return the result of applying the given substitution to the given type
   */
  TypeMirror substitute(TypeMirror type, Map<TypeVariable, TypeMirror> instantiations);

  TypeMirror substitute(TypeMirror type, TypeVariable typeVariable, TypeMirror instantiation);
}
