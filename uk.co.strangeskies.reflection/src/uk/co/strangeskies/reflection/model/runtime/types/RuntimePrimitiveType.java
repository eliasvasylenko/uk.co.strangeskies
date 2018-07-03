package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.PrimitiveType;

/**
 * A specialization of {@link javax.lang.model.type.PrimitiveType} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimePrimitiveType
    extends RuntimeTypeMirror, PrimitiveType, ReifiableRuntimeType {}
