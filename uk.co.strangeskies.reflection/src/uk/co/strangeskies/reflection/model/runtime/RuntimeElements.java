package uk.co.strangeskies.reflection.model.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;

import javax.lang.model.util.Elements;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeAnnotationMirror;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeExecutableElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeModuleElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimePackageElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeTypeParameterElement;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeVariableElement;

public interface RuntimeElements extends Elements {
  RuntimeTypeElement asMirror(Class<?> clazz);

  RuntimePackageElement asMirror(Package pkg);

  RuntimeVariableElement asMirror(Field field);

  RuntimeExecutableElement asMirror(Executable executable);

  RuntimeTypeParameterElement asMirror(TypeVariable<?> typeVariable);

  RuntimeVariableElement asMirror(Parameter parameter);

  RuntimeAnnotationMirror asMirror(Annotation annotation);

  RuntimeModuleElement asMirror(Module module);
}
