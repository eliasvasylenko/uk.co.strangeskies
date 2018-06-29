package uk.co.strangeskies.reflection.model.core.elements.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.core.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.core.StringName;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionElementVisitor;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionTypeParameterElement;
import uk.co.strangeskies.reflection.model.core.types.TypeFactory;

public class ReflectionTypeParameterElementImpl extends ReflectionElementImpl
    implements ReflectionTypeParameterElement {

  private final GenericDeclaration source;
  private final TypeVariable<?> sourceTypeVar;

  protected ReflectionTypeParameterElementImpl(java.lang.reflect.TypeVariable<?> sourceTypeVar) {
    this.sourceTypeVar = Objects.requireNonNull(sourceTypeVar);
    this.source = Objects.requireNonNull(sourceTypeVar.getGenericDeclaration());
  }

  @Override
  public TypeVariable<?> getSource() {
    return sourceTypeVar;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionTypeParameterElementImpl) {
      return sourceTypeVar.equals(((ReflectionTypeParameterElementImpl) o).sourceTypeVar);
    } else {
      return false;
    }
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }

  @Override
  public <R, P> R accept(ReflectionElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }

  @Override
  public List<ReflectionElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public ReflectionElement getEnclosingElement() {
    if (source instanceof Class)
      return CoreReflectionFactory.asMirror((Class<?>) source);
    else if (source instanceof Method)
      return CoreReflectionFactory.asMirror((Method) source);
    else if (source instanceof Constructor)
      return CoreReflectionFactory.asMirror((Constructor<?>) source);
    else
      throw new AssertionError("Unexpected enclosing element: " + source);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.TYPE_PARAMETER;
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(sourceTypeVar.getName());
  }

  // TypeParameterElement methods
  @Override
  public ReflectionElement getGenericElement() {
    return getEnclosingElement(); // As per the doc,
                                  // getEnclosingElement and
                                  // getGenericElement return
                                  // the same information.
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    Type[] types = getSource().getBounds();
    int len = types.length;

    if (len > 0) {
      List<TypeMirror> res = new ArrayList<>(len);
      for (Type t : types) {
        res.add(TypeFactory.instance(t));
      }
      return Collections.unmodifiableList(res);
    } else {
      return Collections.emptyList();
    }
  }

  static List<ReflectionTypeParameterElement> createTypeParameterList(GenericDeclaration source) {
    TypeVariable<?>[] typeParams = source.getTypeParameters();
    int length = typeParams.length;
    if (length == 0)
      return Collections.emptyList();
    else {
      List<ReflectionTypeParameterElement> tmp = new ArrayList<>(length);
      for (TypeVariable<?> typeVar : typeParams)
        tmp.add(new ReflectionTypeParameterElementImpl(typeVar));
      return Collections.unmodifiableList(tmp);
    }
  }
}