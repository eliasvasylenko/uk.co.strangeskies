package uk.co.strangeskies.reflection.model.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;

import javax.lang.model.util.Elements;

import uk.co.strangeskies.reflection.model.core.elements.ReflectionExecutableElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionModuleElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionPackageElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionTypeElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionTypeParameterElement;
import uk.co.strangeskies.reflection.model.core.elements.ReflectionVariableElement;
import uk.co.strangeskies.reflection.model.core.elements.impl.ReflectionAnnotationMirror;

public interface RuntimeElements extends Elements {
  ReflectionTypeElement asMirror(Class<?> clazz);

  ReflectionPackageElement asMirror(Package pkg);

  ReflectionVariableElement asMirror(Field field);

  ReflectionExecutableElement asMirror(Executable executable);

  ReflectionTypeParameterElement asMirror(TypeVariable<?> typeVariable);

  ReflectionVariableElement asMirror(Parameter parameter);

  ReflectionAnnotationMirror asMirror(Annotation annotation);

  ReflectionModuleElement asMirror(Module module);
}
