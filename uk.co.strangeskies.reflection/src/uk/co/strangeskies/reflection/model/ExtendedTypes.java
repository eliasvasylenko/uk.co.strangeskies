package uk.co.strangeskies.reflection.model;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public interface ExtendedTypes extends Types {
  TypeMirrorProxy getProxy();

  TypeMirror getIntersection(TypeMirror... bounds);

  TypeMirror getUnion(TypeMirror... alternatives);
}
