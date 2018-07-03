package uk.co.strangeskies.reflection.model.runtime.types;

import javax.lang.model.type.ReferenceType;

/**
 * A specialization of {@link javax.lang.model.type.ReferenceType} which
 * operates over the core reflection API.
 * 
 * @author Elias N Vasylenko
 */
public interface RuntimeReferenceType extends RuntimeTypeMirror, ReferenceType {}
