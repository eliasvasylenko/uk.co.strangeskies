package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.runtime.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.impl.ModifierUtil;
import uk.co.strangeskies.reflection.model.runtime.impl.StringName;

class ReflectionFieldElementImpl extends ReflectionVariableElementImpl {
  private final Field source;

  protected ReflectionFieldElementImpl(Field source) {
    this.source = Objects.requireNonNull(source);
  }

  @Override
  public Field getSource() {
    return source;
  }

  @Override
  public TypeMirror asType() {
    return CoreReflectionFactory.createTypeMirror(getSource().getType());
  }

  @Override
  public ElementKind getKind() {
    if (source.isEnumConstant())
      return ElementKind.ENUM_CONSTANT;
    else
      return ElementKind.FIELD;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return ModifierUtil
        .instance(source.getModifiers() & java.lang.reflect.Modifier.fieldModifiers(), false);
  }

  @Override
  public Name getSimpleName() {
    return StringName.instance(source.getName());
  }

  @Override
  public RuntimeElement getEnclosingElement() {
    return CoreReflectionFactory.asMirror(source.getDeclaringClass());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ReflectionFieldElementImpl) {
      return Objects.equals(source, ((ReflectionFieldElementImpl) o).getSource());
    } else {
      return false;
    }
  }

  @Override
  public Object getConstantValue() {
    Field target = source;

    // The api says only Strings and primitives may be compile time constants.
    // Ensure field is that, and final.
    //
    // Also, we don't have an instance so restrict to static Fields
    //
    if (!(source.getType().equals(java.lang.String.class) || source.getType().isPrimitive())) {
      return null;
    }
    final int modifiers = target.getModifiers();
    if (!(java.lang.reflect.Modifier.isFinal(modifiers)
        && java.lang.reflect.Modifier.isStatic(modifiers))) {
      return null;
    }

    try {
      return target.get(null);
    } catch (IllegalAccessException e) {
      try {
        target.setAccessible(true);
        return target.get(null);
      } catch (IllegalAccessException i) {
        throw new SecurityException(i);
      }
    }
  }
}