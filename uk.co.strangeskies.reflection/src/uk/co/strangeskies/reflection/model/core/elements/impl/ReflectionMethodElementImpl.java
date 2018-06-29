package uk.co.strangeskies.reflection.model.core.elements.impl;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.core.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.core.ModifierUtil;
import uk.co.strangeskies.reflection.model.core.StringName;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionElement;
import uk.co.strangeskies.reflection.model.core.types.TypeFactory;

class ReflectionMethodElementImpl extends ReflectionExecutableElementImpl {

  protected ReflectionMethodElementImpl(Method source) {
    super(
        Objects.requireNonNull(source),
        ReflectionParameterElementImpl.createParameterList(source));
    this.source = source;
  }

  @Override
  public Method getSource() {
    return (Method) source;
  }

  @Override
  public TypeMirror getReturnType() {
    return TypeFactory.instance(getSource().getReturnType());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionMethodElementImpl) {
      return source.equals(((ReflectionMethodElementImpl) o).getSource());
    } else {
      return false;
    }
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.METHOD;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil
        .instance(
            source.getModifiers() & java.lang.reflect.Modifier.methodModifiers(),
            isDefault());
  }

  @Override
  public ReflectionElement getEnclosingElement() {
    return CoreReflectionFactory.asMirror(source.getDeclaringClass());
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(source.getName());
  }

  @Override
  public AnnotationValue getDefaultValue() {
    Object value = getSource().getDefaultValue();
    if (null == value) {
      return null;
    } else {
      return new ReflectionAnnotationValueImpl(value);
    }
  }

  @Override
  public boolean isDefault() {
    return getSource().isDefault();
  }

  @Override
  public boolean isBridge() {
    return getSource().isBridge();
  }

  @Override
  public ExecutableType asType() {
    return TypeFactory.instance(getSource());
  }
}