package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import static java.util.Objects.requireNonNull;
import static uk.co.strangeskies.reflection.model.runtime.elements.impl.ReflectionParameterElementImpl.createParameterList;

import java.lang.reflect.Constructor;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.impl.ModifierUtil;
import uk.co.strangeskies.reflection.model.runtime.impl.StringName;
import uk.co.strangeskies.reflection.model.runtime.types.impl.RuntimeNoTypeImpl;

public class ConstructorElement extends ReflectionExecutableElementImpl {
  private static final String CONSTRUCTOR_NAME = "<init>";

  public ConstructorElement(Constructor<?> source) {
    super(requireNonNull(source), createParameterList(source));
  }

  @Override
  public Constructor<?> getSource() {
    return (Constructor<?>) source;
  }

  @Override
  public TypeMirror getReturnType() {
    return RuntimeNoTypeImpl.getVoidInstance();
  }

  @Override
  public ExecutableType asType() {
    throw new UnsupportedOperationException(getClass().toString());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ConstructorElement) {
      return source.equals(((ConstructorElement) o).getSource());
    } else {
      return false;
    }
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.CONSTRUCTOR;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil
        .instance(source.getModifiers() & java.lang.reflect.Modifier.constructorModifiers(), false);
  }

  @Override
  public RuntimeElement getEnclosingElement() {
    return CoreReflectionFactory.asMirror(source.getDeclaringClass());
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(CONSTRUCTOR_NAME);
  }

  @Override
  public AnnotationValue getDefaultValue() {
    return null;
  }

  @Override
  public boolean isDefault() {
    return false;
  }
}