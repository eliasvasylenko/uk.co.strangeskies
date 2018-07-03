package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElementVisitor;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeParameterElement;
import uk.co.strangeskies.reflection.model.runtime.impl.StringName;
import uk.co.strangeskies.reflection.model.runtime.types.TypeFactory;

public class ReflectionTypeParameterElementImpl extends ReflectionElementImpl
    implements RuntimeTypeParameterElement {
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
  public <R, P> R accept(RuntimeElementVisitor<R, P> v, P p) {
    return v.visitTypeParameter(this, p);
  }

  @Override
  public List<RuntimeElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public RuntimeElement getEnclosingElement() {
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

  @Override
  public RuntimeElement getGenericElement() {
    return getEnclosingElement();
  }

  @Override
  public List<? extends TypeMirror> getBounds() {
    return Stream.of(getSource().getBounds()).map(TypeFactory::instance).collect(toList());
  }

  public static List<RuntimeTypeParameterElement> createTypeParameterList(
      GenericDeclaration source) {
    return Stream
        .of(source.getTypeParameters())
        .map(ReflectionTypeParameterElementImpl::new)
        .collect(toList());
  }
}