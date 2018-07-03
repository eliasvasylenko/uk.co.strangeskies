package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.NullType;

/**
 * A specialization of {@link javax.lang.model.type.NullType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeNullType extends RuntimeReferenceType, NullType {}
