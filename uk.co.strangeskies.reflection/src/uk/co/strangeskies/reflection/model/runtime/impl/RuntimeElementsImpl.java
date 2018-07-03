package uk.co.strangeskies.reflection.model.runtime.impl;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import uk.co.strangeskies.reflection.model.runtime.RuntimeElements;
import uk.co.strangeskies.reflection.model.runtime.RuntimeModel;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeAnnotationMirror;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeExecutableElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeModuleElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimePackageElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeParameterElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeVariableElement;

public class RuntimeElementsImpl implements RuntimeElements {
  public RuntimeElementsImpl(RuntimeModel model) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public PackageElement getPackageElement(CharSequence name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeElement getTypeElement(CharSequence name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
      AnnotationMirror a) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDocComment(Element e) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isDeprecated(Element e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Name getBinaryName(TypeElement type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PackageElement getPackageOf(Element type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends Element> getAllMembers(TypeElement type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hides(Element hider, Element hidden) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean overrides(
      ExecutableElement overrider,
      ExecutableElement overridden,
      TypeElement type) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getConstantExpression(Object value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void printElements(Writer w, Element... elements) {
    // TODO Auto-generated method stub

  }

  @Override
  public Name getName(CharSequence cs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isFunctionalInterface(TypeElement type) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RuntimeTypeElement asMirror(Class<?> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimePackageElement asMirror(Package pkg) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeVariableElement asMirror(Field field) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeExecutableElement asMirror(Executable executable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeTypeParameterElement asMirror(TypeVariable<?> typeVariable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeVariableElement asMirror(Parameter parameter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeAnnotationMirror asMirror(Annotation annotation) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RuntimeModuleElement asMirror(Module module) {
    // TODO Auto-generated method stub
    return null;
  }
}
