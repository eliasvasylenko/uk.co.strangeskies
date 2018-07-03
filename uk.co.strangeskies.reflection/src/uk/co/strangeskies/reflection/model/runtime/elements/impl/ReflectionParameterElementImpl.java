package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.impl.ModifierUtil;
import uk.co.strangeskies.reflection.model.runtime.impl.StringName;

class ReflectionParameterElementImpl extends ReflectionVariableElementImpl {
  private final Parameter source;

  protected ReflectionParameterElementImpl(Parameter source) {
    this.source = Objects.requireNonNull(source);
  }

  @Override
  public Parameter getSource() {
    return source;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil
        .instance(source.getModifiers() & java.lang.reflect.Modifier.parameterModifiers(), false);
  }

  @Override
  public TypeMirror asType() {
    // TODO : switch to parameterized type
    return CoreReflectionFactory.createTypeMirror(source.getType());
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.PARAMETER;
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(source.getName());
  }

  @Override
  public RuntimeElement getEnclosingElement() {
    Executable enclosing = source.getDeclaringExecutable();
    if (enclosing instanceof Method)
      return CoreReflectionFactory.asMirror((Method) enclosing);
    else if (enclosing instanceof Constructor)
      return CoreReflectionFactory.asMirror((Constructor<?>) enclosing);
    else
      throw new AssertionError("Bad enclosing value.");
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionParameterElementImpl) {
      return source.equals(((ReflectionParameterElementImpl) o).getSource());
    } else
      return false;
  }

  // VariableElement methods
  @Override
  public Object getConstantValue() {
    return null;
  }

  @Override
  public boolean isSynthetic() {
    return source.isSynthetic();
  }

  @Override
  public boolean isImplicit() {
    return source.isImplicit();
  }

  static List<ReflectionParameterElementImpl> createParameterList(Executable source) {
    return Stream
        .of(source.getParameters())
        .map(ReflectionParameterElementImpl::new)
        .collect(toList());
  }
}