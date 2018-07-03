package uk.co.strangeskies.reflection.model.runtime.types;

import java.util.List;

import javax.lang.model.type.DeclaredType;

import uk.co.strangeskies.reflection.model.runtime.elements.RuntimeElement;

/**
 * A specialization of {@link javax.lang.model.type.DeclaredType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeDeclaredType
    extends RuntimeReferenceType, DeclaredType, ReifiableRuntimeType {
  @Override
  RuntimeElement asElement();

  @Override
  RuntimeTypeMirror getEnclosingType();

  @Override
  List<ReifiableRuntimeType> getTypeArguments();
}
