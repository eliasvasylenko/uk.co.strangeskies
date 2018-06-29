package uk.co.strangeskies.reflection.model.core.elements.impl;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import uk.co.strangeskies.reflection.model.core.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionElementVisitor;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionExecutableElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionTypeParameterElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionVariableElement;

abstract class ReflectionExecutableElementImpl extends ReflectionElementImpl
    implements ReflectionExecutableElement {
  protected Executable source = null;
  protected final List<ReflectionParameterElementImpl> parameters;

  protected ReflectionExecutableElementImpl(
      Executable source,
      List<ReflectionParameterElementImpl> parameters) {
    this.source = Objects.requireNonNull(source);
    this.parameters = Objects.requireNonNull(parameters);
  }

  @Override
  public Executable getSource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public <R, P> R accept(ReflectionElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public abstract ExecutableType asType();

  // Only Types and Packages enclose elements; see Element.getEnclosedElements()
  @Override
  public List<ReflectionElement> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public List<ReflectionVariableElement> getParameters() {
    List<ReflectionVariableElement> tmp = new ArrayList<>();
    for (ReflectionVariableElement parameter : parameters) {
      if (!parameter.isSynthetic())
        tmp.add(parameter);
    }
    return tmp;
  }

  @Override
  public List<ReflectionVariableElement> getAllParameters() {
    // Could "fix" this if the return type included wildcards
    @SuppressWarnings("unchecked")
    List<ReflectionVariableElement> tmp = (List) parameters;
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
    return source.isVarArgs();
  }

  @Override
  public boolean isSynthetic() {
    return source.isSynthetic();
  }

  @Override
  public boolean isBridge() {
    return false;
  }

  @Override
  public List<ReflectionTypeParameterElement> getTypeParameters() {
    return ReflectionTypeParameterElementImpl.createTypeParameterList(source);
  }

  @Override
  public abstract AnnotationValue getDefaultValue();

  @Override
  public TypeMirror getReceiverType() {
    // New in JDK 8
    throw new UnsupportedOperationException(this.toString());
  }
}