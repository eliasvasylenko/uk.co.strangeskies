package uk.co.strangeskies.reflection.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class IntersectionReferenceTypeImpl extends ReflectionTypeMirror implements IntersectionReferenceType {
  private Type[] sources = null;

  IntersectionReferenceTypeImpl(Type[] sources) {
    super(TypeKind.INTERSECTION);
    this.sources = Arrays.copyOf(Objects.requireNonNull(sources), sources.length);
  }

  @Override
  public List<? extends TypeMirror> directSuperTypes() {
    int len = sources.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<TypeMirror>(len);
      for (Type c : sources) {
        res.add(TypeFactory.instance(c));
      }
      return Collections.unmodifiableList(res);
    } else {
      return Collections.emptyList();
    }
  }
}