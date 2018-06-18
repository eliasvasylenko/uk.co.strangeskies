package uk.co.strangeskies.reflection.model;

import java.util.List;
import java.util.Objects;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;

class ReflectionPackageElementImpl extends ReflectionElementImpl
    implements ReflectionPackageElement {

  private final Package source;

  protected ReflectionPackageElementImpl(Package source) {
    this.source = source;
  }

  @Override
  public Package getSource() {
    return source;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }

  @Override
  public <R, P> R accept(ReflectionElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionPackageElementImpl) {
      return Objects.equals(source, ((ReflectionPackageElementImpl) o).getSource());
    } else {
      return false;
    }
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.PACKAGE;
  }

  @Override
  public ReflectionElement getEnclosingElement() {
    return null;
  }

  @Override
  public List<ReflectionElement> getEnclosedElements() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Name getQualifiedName() {
    return StringName.instance((source != null) ? source.getName() : "");
  }

  @Override
  public Name getSimpleName() {
    String n = ((source != null) ? source.getName() : "");
    int index = n.lastIndexOf('.');
    if (index > 0) {
      return StringName.instance(n.substring(index + 1, n.length()));
    } else {
      return StringName.instance(n);
    }
  }

  @Override
  public boolean isUnnamed() {
    if (source != null) {
      String name = source.getName();
      return (name == null || name.isEmpty());
    } else
      return true;
  }
}