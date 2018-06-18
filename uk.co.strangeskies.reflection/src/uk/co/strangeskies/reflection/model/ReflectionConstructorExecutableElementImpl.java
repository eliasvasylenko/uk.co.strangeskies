package uk.co.strangeskies.reflection.model;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

class ReflectionConstructorExecutableElementImpl extends ReflectionExecutableElementImpl {

  protected ReflectionConstructorExecutableElementImpl(Constructor<?> source) {
    super(
        Objects.requireNonNull(source),
        ReflectionParameterElementImpl.createParameterList(source));
  }

  @Override
  public Constructor<?> getSource() {
    return (Constructor<?>) source;
  }

  @Override
  public TypeMirror getReturnType() {
    return ReflectionNoType.getVoidInstance();
  }

  @Override
  public ExecutableType asType() {
    throw new UnsupportedOperationException(getClass().toString());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionConstructorExecutableElementImpl) {
      return source.equals(((ReflectionConstructorExecutableElementImpl) o).getSource());
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
  public ReflectionElement getEnclosingElement() {
    return CoreReflectionFactory.createMirror(source.getDeclaringClass());
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance("<init>");
  }

  @Override
  public AnnotationValue getDefaultValue() {
    // a constructor is never an annotation element
    return null;
  }

  @Override
  public boolean isDefault() {
    return false; // A constructor cannot be a default method
  }
}