package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static uk.co.strangeskies.reflection.model.runtime.elements.impl.ReflectionTypeParameterElementImpl.createTypeParameterList;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElementVisitor;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeExecutableElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeParameterElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeVariableElement;

public abstract class ReflectionExecutableElementImpl extends ReflectionElementImpl
    implements RuntimeExecutableElement {
  protected final List<ReflectionParameterElementImpl> parameters;

  protected ReflectionExecutableElementImpl(List<ReflectionParameterElementImpl> parameters) {
    this.parameters = unmodifiableList(requireNonNull(parameters));
  }

  @Override
  public abstract Executable getSource();

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public <R, P> R accept(RuntimeElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public abstract ExecutableType asType();

  // Only Types and Packages enclose elements; see Element.getEnclosedElements()
  @Override
  public List<RuntimeElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public List<RuntimeVariableElement> getParameters() {
    List<RuntimeVariableElement> tmp = new ArrayList<>();
    for (RuntimeVariableElement parameter : parameters) {
      if (!parameter.isSynthetic())
        tmp.add(parameter);
    }
    return tmp;
  }

  @Override
  public List<RuntimeVariableElement> getAllParameters() {
    // Could "fix" this if the return type included wildcards
    @SuppressWarnings("unchecked")
    List<RuntimeVariableElement> tmp = (List) parameters;
    return tmp;
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    Class<?>[] thrown = source.getExceptionTypes();
    int len = thrown.length;
    List<TypeMirror> res = new ArrayList<>(len);

    if (len > 0) {
      for (Class<?> c : thrown) {
        res.add(CoreReflectionFactory.createTypeMirror(c));
      }
    } else {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(res);
  }

  @Override
  public boolean isVarArgs() {
    return getSource().isVarArgs();
  }

  @Override
  public boolean isSynthetic() {
    return getSource().isSynthetic();
  }

  @Override
  public boolean isBridge() {
    return false;
  }

  @Override
  public List<RuntimeTypeParameterElement> getTypeParameters() {
    return createTypeParameterList(getSource());
  }

  @Override
  public abstract AnnotationValue getDefaultValue();

  @Override
  public TypeMirror getReceiverType() {
    return CoreReflectionFactory.createTypeMirror(getSource().getAnnotatedReceiverType());
  }
}