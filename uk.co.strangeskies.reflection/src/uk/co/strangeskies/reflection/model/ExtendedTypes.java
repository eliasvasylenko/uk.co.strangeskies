package uk.co.strangeskies.reflection.model;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public interface ExtendedTypes extends Types {
  TypeMirrorProxy getProxy();

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
  TypeMirror getIntersection(TypeMirror... lowerBounds);

  /**
   * Derive the greatest lower bound for a set of types, as defined in the Java
   * language specification.
   * 
   * @param alternatives
   *          A collection of types representing the lower bounds of an unknown
   *          type.
   * @return The most specific single type which, as a lower bound, will also
   *         satisfy each lower bound in the given set.
   */
  TypeMirror getUnion(TypeMirror... alternatives);
}
