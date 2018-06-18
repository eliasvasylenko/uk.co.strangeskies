package uk.co.strangeskies.reflection.model;

import static java.util.Arrays.asList;

import java.util.Collection;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public interface ExtendedTypes extends Types {
  TypeMirrorProxy getProxy();

  /**
   * See {@link ExtendedTypes#getIntersection(Collection)}.
   * 
   * @param lowerBounds
   *          Forwards to {@code lowerBounds} parameter.
   * @return As referenced method.
   */
  default TypeMirror getIntersection(TypeMirror... lowerBounds) {
    return getIntersection(asList(lowerBounds));
  }

  /**
   * Derive the greatest lower bound for a set of types, as defined in the Java
   * language specification.
   * 
   * @param lowerBounds
   *          A collection of types representing the lower bounds of an unknown
   *          type.
   * @return The most specific single type which, as a lower bound, will also
   *         satisfy each lower bound in the given set.
   */
  TypeMirror getIntersection(Collection<? extends TypeMirror> lowerBounds);

  /**
   * See {@link ExtendedTypes#getIntersection(Collection)}.
   * 
   * @param lowerBounds
   *          Forwards to {@code lowerBounds} parameter.
   * @return As referenced method.
   */
  default TypeMirror getUnion(TypeMirror... lowerBounds) {
    return getIntersection(asList(lowerBounds));
  }

  /**
   * Derive the greatest lower bound for a set of types, as defined in the Java
   * language specification.
   * 
   * @param lowerBounds
   *          A collection of types representing the lower bounds of an unknown
   *          type.
   * @return The most specific single type which, as a lower bound, will also
   *         satisfy each lower bound in the given set.
   */
  TypeMirror getUnion(Collection<? extends TypeMirror> lowerBounds);
}
