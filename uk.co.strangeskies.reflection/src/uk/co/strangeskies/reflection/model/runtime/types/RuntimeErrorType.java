package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.ErrorType;

/**
 * A specialization of {@link javax.lang.model.type.ErrorType} which operates
 * over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeErrorType extends RuntimeReferenceType, ErrorType {}
