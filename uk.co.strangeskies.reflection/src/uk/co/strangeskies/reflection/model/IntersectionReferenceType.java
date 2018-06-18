package uk.co.strangeskies.reflection.model;

import java.util.List;

import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeMirror;

public interface IntersectionReferenceType extends ReferenceType {
  List<? extends TypeMirror> directSuperTypes();
}
