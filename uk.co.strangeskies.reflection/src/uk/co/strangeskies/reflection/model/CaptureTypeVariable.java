package uk.co.strangeskies.reflection.model;

import java.util.List;
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class CaptureTypeVariable extends ReflectionTypeMirror
    implements javax.lang.model.type.TypeVariable {
  private TypeMirror source = null;
  private TypeMirror upperBound = null;
  private TypeMirror lowerBound = null;

  CaptureTypeVariable(TypeMirror source, TypeMirror upperBound, TypeMirror lowerBound) {
    super(TypeKind.TYPEVAR);

    this.source = Objects.requireNonNull(source);
    this.upperBound = (upperBound == null ? ReflectionTypes.instance().getNullType() : upperBound);
    this.lowerBound = (lowerBound == null ? ReflectionTypes.instance().getNullType() : lowerBound);
  }

  protected Class<?> getSource() {
    if (source instanceof ReflectionDeclaredType) {
      return ((ReflectionDeclaredType) source).getSource();
    } else {
      return null;
    }
  }

  @Override
  public TypeMirror getUpperBound() {
    return upperBound;
  }

  @Override
  public TypeMirror getLowerBound() {
    return lowerBound;
  }

  @Override
  public Element asElement() {
    if (null == getSource()) {
      return null;
    }
    return CoreReflectionFactory.createMirror(getSource());
  }

  @Override
  List<? extends TypeMirror> directSuperTypes() {
    throw new UnsupportedOperationException();

  }

  @Override
  public String toString() {
    return getKind() + " CAPTURE of: " + source.toString();
  }
}