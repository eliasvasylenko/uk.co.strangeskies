package uk.co.strangeskies.reflection.model.runtime.elements.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.DeclaredType;

import sun.reflect.generics.factory.CoreReflectionFactory;
import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeExecutableElement;
import uk.co.strangeskies.reflection.model.runtime.types.TypeFactory;

public class ReflectionAnnotationMirror implements javax.lang.model.element.AnnotationMirror {
  private final Annotation annotation;

  public ReflectionAnnotationMirror(Annotation annotation) {
    this.annotation = Objects.requireNonNull(annotation);
  }

  @Override
  public DeclaredType getAnnotationType() {
    return (DeclaredType) TypeFactory.instance(annotation.annotationType());
  }

  @Override
  public Map<? extends RuntimeExecutableElement, ? extends AnnotationValue> getElementValues() {
    // This differs from the javac implementation in that it returns default values

    Method[] elems = annotation.annotationType().getDeclaredMethods();
    int len = elems.length;

    if (len > 0) {
      Map<RuntimeExecutableElement, AnnotationValue> res = new HashMap<>();
      for (Method m : elems) {
        AnnotationValue v;
        try {
          v = new ReflectionAnnotationValueImpl(m.invoke(annotation));
        } catch (IllegalAccessException e) {
          try {
            m.setAccessible(true);
            v = new ReflectionAnnotationValueImpl(m.invoke(annotation));
          } catch (IllegalAccessException i) {
            throw new SecurityException(i);
          } catch (InvocationTargetException ee) {
            throw new RuntimeException(ee);
          }
        } catch (InvocationTargetException ee) {
          throw new RuntimeException(ee);
        }
        RuntimeExecutableElement e = CoreReflectionFactory.asMirror(m);
        res.put(e, v);
      }

      return Collections.unmodifiableMap(res);
    } else {
      return Collections.emptyMap();
    }
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ReflectionAnnotationMirror) {
      return annotation.equals(((ReflectionAnnotationMirror) other).annotation);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(annotation);
  }

  @Override
  public String toString() {
    return annotation.toString();
  }
}