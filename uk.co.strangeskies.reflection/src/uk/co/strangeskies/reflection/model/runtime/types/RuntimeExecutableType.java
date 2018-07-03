package uk.co.strangeskies.reflection.model.runtime.types;

import java.lang.reflect.Executable;
import java.util.List;

import javax.lang.model.type.ExecutableType;

/**
 * A specialization of {@link javax.lang.model.type.ExecutableType} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeExecutableType extends RuntimeTypeMirror, ExecutableType {
  Executable getErasedSource();

  @Override
  List<RuntimeTypeVariable> getTypeVariables();

  @Override
  RuntimeTypeMirror getReturnType();

  @Override
  List<RuntimeTypeMirror> getParameterTypes();

  @Override
  RuntimeTypeMirror getReceiverType();

  @Override
  List<RuntimeTypeMirror> getThrownTypes();
}
